package proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Proxy implements Runnable{
    private final Selector selector;
    private final Utilities utilitor;

    public static int BUF_SIZE = (int) Math.pow(2, 20);

    public Proxy(String host, int port) throws IOException {
        utilitor = new Utilities(host, port);
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        selector = Selector.open();
        serverChannel.socket().bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, serverChannel.validOps());
        System.out.println("Proxy started at " + port + " port");
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                selector.select();
            } catch (IOException e) {
                continue;
            }
            if (!selector.isOpen()){
                continue;
            }
            Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
            while (keysIterator.hasNext()) {
                SelectionKey key = keysIterator.next();
                keysIterator.remove();
                if (!key.isValid()){
                    continue;
                }
                try {
                    if (key.isAcceptable()) {
                        accept(key);
                    }
                    else if (key.isConnectable()) {
                        connect(key);
                    }
                    else if (key.isReadable()) {
                        read(key);
                    }
                    else if (key.isWritable()) {
                        write(key);
                    }
                } catch (Exception e) {
                    close(key);
                }
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(key.selector(), SelectionKey.OP_READ);
    }

    private void close(SelectionKey key) {
        try {
            key.channel().close();
        } catch (IOException ignored) {}

        SelectionKey selectionKey = ((Attachment) key.attachment()).key;
        if (selectionKey != null) {
            ((Attachment) selectionKey.attachment()).key = null;
            if (selectionKey.isValid()) {
                if ((selectionKey.interestOps() & SelectionKey.OP_WRITE) == 0) {
                    ((Attachment) selectionKey.attachment()).out.flip();
                }
                selectionKey.interestOps(SelectionKey.OP_WRITE);
            }
        }
        key.cancel();
    }

    private void connect(SelectionKey key) throws IOException {
        Attachment attachment = (Attachment) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        channel.finishConnect();
        attachment.in = ByteBuffer.allocate(BUF_SIZE);
        attachment.in.put(utilitor.createResponse()).flip();
        attachment.out = ((Attachment)attachment.key.attachment()).in;
        ((Attachment)attachment.key.attachment()).out = attachment.in;
        ((Attachment)attachment.key.attachment()).status = Attachment.Status.TRANSITING;
        attachment.status = Attachment.Status.TRANSITING;
        attachment.key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        key.interestOps(0);
    }

    private void read(SelectionKey key) throws IOException {
        Attachment attachment = (Attachment) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        if (attachment == null) {
            attachment = new Attachment();
            attachment.in = ByteBuffer.allocate(BUF_SIZE);
            key.attach(attachment);
        }
        if (channel.read(attachment.in) < 0) {
            close(key);
        }
        if (attachment.status == Attachment.Status.DISCONNECTED){
            utilitor.connectToServer(key);
        }
        else if (attachment.status == Attachment.Status.CONNECTED){
            byte[] clientRequest = attachment.in.array();
            byte[] response = utilitor.createResponse();
            if (clientRequest[0] == Codes.SOCKS5_VERSION){
                if (clientRequest[1] == Codes.ClientRequest.TCP_CONNECTION){
                    byte type = clientRequest[3];
                    String address = utilitor.getAddress(clientRequest, type);
                    if (address == null){
                        response[1] = type == Codes.IPv4_CONNECTION? Codes.ServerResponse.HOST_UNAVAILABLE :
                                type == Codes.DOMAIN_CONNECTION? Codes.ServerResponse.NETWORK_UNAVAILABLE : Codes.ServerResponse.ADDRESS_TYPE_NOT_SUPPORTED;
                        channel.write(ByteBuffer.wrap(response));
                        String msg = type == Codes.IPv4_CONNECTION? "Unable to connect to host" : "Failed to find domain";
                        throw new IOException(msg);
                    }
                    short clientPort = utilitor.getClientPort(clientRequest);
                    try {
                        SocketAddress socketAddress = new InetSocketAddress(address, clientPort);
                        SocketChannel socketChannel;
                        socketChannel = SocketChannel.open();
                        socketChannel.configureBlocking(false);
                        socketChannel.connect(socketAddress);
                        SelectionKey targetKey = socketChannel.register(selector, SelectionKey.OP_CONNECT);
                        ((Attachment)key.attachment()).key = targetKey;
                        key.interestOps(0);
                        Attachment target = new Attachment();
                        target.key = key;
                        targetKey.attach(target);
                        attachment.in.clear();
                        System.out.println("Requested connection through domain: " + address);
                    } catch (IOException e) {
                        response[1] = Codes.ServerResponse.CONNECTION_FAILURE;
                        channel.write(ByteBuffer.wrap(response));
                        throw new IOException("The host: " + address + " dropped the connection");
                    }
                }
                else {
                    response[1] = Codes.ServerResponse.INVALID_CONNECTION;
                    channel.write(ByteBuffer.wrap(response));
                    close(key);
                }
            }
        }
        else if (attachment.status == Attachment.Status.TRANSITING){
            attachment.key.interestOps(attachment.key.interestOps() | SelectionKey.OP_WRITE);
            key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
            attachment.in.flip();
        }
    }

    private void write(SelectionKey key) throws IOException {
        Attachment attachment = (Attachment) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        if (!key.isValid()){
            return;
        }
        channel.write(attachment.out);
        if (attachment.out.remaining() == 0) {
            if (attachment.key == null) {
                close(key);
            }
            else {
                attachment.out.clear();
                attachment.key.interestOps(attachment.key.interestOps() | SelectionKey.OP_READ);
                key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
            }
        }
    }
}
