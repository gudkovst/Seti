package proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class Utilities {
    private final String hostname;
    private final int portNum;
    private int domainNameLength;

    public static int HEADER_LENGTH = 4;
    public static int IP_LENGTH = 4;
    public static int PORT_LENGTH = 2;

    public static int GREETING_LENGTH = 2;

    public Utilities(String host, int port){
        hostname = host;
        portNum = port;
    }

    public byte[] createResponse(){
        byte[] response = new byte[HEADER_LENGTH + IP_LENGTH + PORT_LENGTH];
        byte[] header = makeHeader();
        byte[] host = hostname.getBytes();
        byte[] port = ByteBuffer.allocate(PORT_LENGTH).order(ByteOrder.BIG_ENDIAN).putShort((short) portNum).array();
        System.arraycopy(header, 0, response, 0, HEADER_LENGTH);
        System.arraycopy(host, 0, response, HEADER_LENGTH, IP_LENGTH);
        System.arraycopy(port, 0, response, HEADER_LENGTH + IP_LENGTH, PORT_LENGTH);
        return response;
    }

    private byte[] makeHeader(){
        byte[] header = new byte[HEADER_LENGTH];
        header[0] = Codes.SOCKS5_VERSION;
        header[1] = Codes.ServerResponse.SUCCESSFUL_REQUEST;
        header[2] = 0x00; // reserved byte
        header[3] = Codes.IPv4_CONNECTION;
        return header;
    }

    public short getClientPort(byte[] clientRequest) throws IOException {
        if (clientRequest[3] == Codes.IPv4_CONNECTION){
            return ByteBuffer.wrap(Arrays.copyOfRange(clientRequest, HEADER_LENGTH + IP_LENGTH, HEADER_LENGTH + IP_LENGTH + PORT_LENGTH)).getShort();
        }
        else if (clientRequest[3] == Codes.DOMAIN_CONNECTION) {
            return ByteBuffer.wrap(Arrays.copyOfRange(clientRequest, 5 + domainNameLength, 5 + domainNameLength + PORT_LENGTH)).getShort();
        }
        throw new IOException("IPv6 not supported");
    }

    public void connectToServer(SelectionKey key) throws IOException {
        Attachment attachment = (Attachment) key.attachment();
        byte[] clientGreeting = attachment.in.array();
        if (clientGreeting[0] == Codes.SOCKS5_VERSION) {
            int numbOfMethods = clientGreeting[1];
            int lastMethodAddr = numbOfMethods + 2;
            boolean acceptableMethod = false;
            for (int i = 2; i < lastMethodAddr; i++) {
                if (Arrays.asList(Codes.ServerResponse.ACCEPTABLE_METHODS).contains(clientGreeting[i])) {
                    acceptableMethod = true;
                    sendGreeting(key, clientGreeting[i]);
                    attachment.status = Attachment.Status.CONNECTED;
                    attachment.in.clear();
                    break;
                }
            }
            if (!acceptableMethod) {
                sendGreeting(key, Codes.ServerResponse.NO_ACCEPTABLE_METHODS);
                throw new IOException("No supported method found");
            }
        }
        else {
            sendGreeting(key, Codes.ServerResponse.NO_ACCEPTABLE_METHODS);
            throw new IOException("SOCKS < SOCKS5 not supported");
        }
    }

    private void sendGreeting(SelectionKey key, byte responseByte) throws IOException {
        byte[] response = new byte[GREETING_LENGTH];
        response[0] = Codes.SOCKS5_VERSION;
        response[1] = responseByte;
        ((SocketChannel) key.channel()).write(ByteBuffer.allocate(GREETING_LENGTH).put(response).flip());
    }

    public String getAddress(byte[] clientRequest, byte type){
        String address = null;
        int addressLength = IP_LENGTH;
        int startByte = HEADER_LENGTH;
        if (type == Codes.DOMAIN_CONNECTION) {
            addressLength = clientRequest[IP_LENGTH];
            domainNameLength = addressLength;
            startByte++;
        }
        byte[] rowAddress = Arrays.copyOfRange(clientRequest, startByte, startByte + addressLength);
        try {
            if (type == Codes.IPv4_CONNECTION) {
                address = InetAddress.getByAddress(rowAddress).getHostAddress();
            }
            else if (type == Codes.DOMAIN_CONNECTION) {
                String ip = new String(rowAddress);
                address = InetAddress.getByName(ip).getHostAddress();
            }
        } catch (UnknownHostException ignored) {}
        return address;
    }
}
