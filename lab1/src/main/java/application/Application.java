import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.*;

public class Application {
    private static final int TTL = 6000; //время жизни записи
    private static final int TCL = 5000; //время между подтверждениями записи
    private static final int MAX_SIZE = 1;  //максимальный размер пакета
    private static final int ERT = 2000; //время между запросами о выходе
    private static final byte secretKey = 113;

    private final InetAddress addrMultiGroup;
    private final int port;
    private Thread receiver;
    private MulticastSocket multicastSocket;

    public static void main(String[] args) throws IOException {
        if (args.length < 2){
            System.out.print(InetAddress.getLocalHost().toString());
            System.err.print("Too few arguments\n");
            return;
        }
        Application application = new Application(args[0], Integer.parseInt(args[1]));
        application.execute();
    }

    public Application(String address, int port) throws UnknownHostException {
        addrMultiGroup = InetAddress.getByName(address);
        this.port = port;
    }

    public void execute() throws IOException {
        multicastSocket = new MulticastSocket(port);
        multicastSocket.joinGroup(addrMultiGroup);
        Timer sendTimer = new Timer();
        sendTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, TCL);
        receiving();
        Scanner scanner = new Scanner(System.in);
        Timer exitTimer = new Timer();
        System.out.print("enter x to exit\n");
        exitTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (scanner.hasNext())
                    if ("x".equals(scanner.next())) {
                        try {
                            exit();
                            scanner.close();
                            System.exit(0);
                        } catch (InterruptedException | IOException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }, ERT, ERT);
    }

    private void send() throws IOException {
        byte[] msg = {coding(InetAddress.getLocalHost())};
        DatagramPacket pack = new DatagramPacket(msg, msg.length, addrMultiGroup, port);
        multicastSocket.send(pack);
    }

    private void receiving() {
        receiver = new Thread(() -> {
            Map<String, Long> copiesAppl = new HashMap<>();
            while (!Thread.currentThread().isInterrupted()){
                InetAddress newApplAddr = null;
                try {
                    newApplAddr = receive(multicastSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (newApplAddr != null)
                    if (copiesAppl.put(newApplAddr.toString(), System.currentTimeMillis()) == null)
                        for (String copy : copiesAppl.keySet()) {
                            if (isValid(copiesAppl.get(copy)))
                                System.out.println(copy);
                            else
                                copiesAppl.remove(copy);
                        }
            }
        });
        receiver.start();
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

    private byte coding(InetAddress addr) {
        byte[] ip = addr.getAddress();
        byte code = secretKey;
        for (byte b: ip)
            code ^= b;
        return code;
    }

    private void exit() throws InterruptedException, IOException {
        receiver.interrupt();
        receiver.join();
        multicastSocket.leaveGroup(addrMultiGroup);
    }
}
