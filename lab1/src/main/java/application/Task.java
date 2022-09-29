package application;

import java.net.InetAddress;
import java.net.MulticastSocket;

abstract class Task implements Runnable{
    private static final byte secretKey = 113;
    protected MulticastSocket multicastSocket;

    protected byte coding(InetAddress addr) {
        byte[] ip = addr.getAddress();
        byte code = secretKey;
        for (byte b: ip)
            code ^= b;
        return code;
    }
}
