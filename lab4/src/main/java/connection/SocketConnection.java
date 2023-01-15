package connection;

import application.Config;
import snakes.SnakesProto;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class SocketConnection implements Connection {
    private final DatagramSocket socket;
    private final MulticastSocket multicastSocket;

    public SocketConnection() throws IOException {
        socket = new DatagramSocket(0);
        socket.setSoTimeout(Config.aliveTime);
        multicastSocket = new MulticastSocket(Config.multicastPort);
        multicastSocket.joinGroup(InetAddress.getByName(Config.multicastAddress));
    }

    @Override
    public void send(byte[] msg, InetAddress to, int port) {
        DatagramPacket datagram = new DatagramPacket(msg, msg.length, to, port);
        try {
            socket.send(datagram);
            SnakesProto.GameMessage message = SnakesProto.GameMessage.parseFrom(msg);
            System.out.println("send " + message.getTypeCase().name() + message.getMsgSeq() + " to " + port);
        } catch (IOException ignored) {}
    }

    @Override
    public Message recv() {
        return receive(socket);
    }

    @Override
    public Message listenMulticast() {
        return receive(multicastSocket);
    }

    private Message receive(DatagramSocket socket){
        DatagramPacket datagram = new DatagramPacket(new byte[Config.maxSizeMessage], Config.maxSizeMessage);
        try {
            socket.receive(datagram);
            byte[] msgData = Arrays.copyOf(datagram.getData(), datagram.getLength());
            return new Message(msgData, datagram.getAddress(), datagram.getPort());
        } catch (IOException e) {
            return null;
        }
    }
}
