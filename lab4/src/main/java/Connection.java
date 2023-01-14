package connection;

import java.net.InetAddress;

public interface Connection {
    public void send(byte[] msg, InetAddress to, int port);
    public Message recv();
    public Message listenMulticast();
}
