package application;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;

public final class ReceiveTask extends Task {
    private static final int TTL = 6000; //время жизни записи
    private static final int MAX_SIZE = 1;  //максимальный размер пакета

    public ReceiveTask(MulticastSocket socket){
        multicastSocket = socket;
    }

    @Override
    public void run() {
        Map<String, Long> copiesAppl = new HashMap<>();
        while (!Thread.currentThread().isInterrupted()) {
            InetAddress newApplAddr = null;
            try {
                newApplAddr = receive(multicastSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (newApplAddr != null) {
                if (copiesAppl.put(newApplAddr.toString(), System.currentTimeMillis()) == null) {
                    for (String copy : copiesAppl.keySet()) {
                        if (isValid(copiesAppl.get(copy)))
                            System.out.println(copy);
                        else
                            copiesAppl.remove(copy);
                    }
                }
            }
        }
    }

    private InetAddress receive(MulticastSocket socket) throws IOException {
        DatagramPacket recPack = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
        socket.receive(recPack);
        return isValid(recPack)? recPack.getAddress() : null;
    }

    private boolean isValid(DatagramPacket packet) {
        return getCode(packet) == coding(packet.getAddress());
    }

    private boolean isValid(Long confirmationTime){
        long now = System.currentTimeMillis();
        return (now - confirmationTime) <= TTL;
    }

    private byte getCode(DatagramPacket packet){
        byte[] data = packet.getData();
        return data[data.length - 1];
    }
}
