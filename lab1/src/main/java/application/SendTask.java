package application;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public final class SendTask extends Task{
    private final InetAddress addrMultiGroup;
    private final int port;

    public SendTask(InetAddress address, int port, MulticastSocket socket){
        multicastSocket = socket;
        addrMultiGroup = address;
        this.port = port;
    }

    @Override
    public void run(){
        byte[] msg = new byte[0];
        try {
            msg = new byte[]{coding(InetAddress.getLocalHost())};
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        DatagramPacket pack = new DatagramPacket(msg, msg.length, addrMultiGroup, port);
        try {
            multicastSocket.send(pack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
