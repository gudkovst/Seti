package connection;

import lombok.Getter;

import java.net.InetAddress;

@Getter
public class Message {
    private final byte[] message;
    private InetAddress address;
    private int port;

    public Message(byte[] msg, InetAddress address, int port){
        this.message = msg;
        this.address = address;
        this.port = port;
    }

    public Message(byte[] msg){
        message = msg;
    }
}
