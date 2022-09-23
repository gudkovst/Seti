package application;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Application {

    private static final int TCL = 5; //время между подтверждениями записи
    private static final int ERT = 2; //время между запросами о выходе
    private static final int NUM_TIMERS = 2;

    private final InetAddress addrMultiGroup;
    private final int port;

    public static void main(String[] args) throws IOException {
        if (args.length < 2){
            System.err.print("Too few arguments.\n Check: [IP multicast group] [port]\n");
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
        MulticastSocket multicastSocket = new MulticastSocket(port);
        multicastSocket.joinGroup(addrMultiGroup);
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(NUM_TIMERS);
        ExecutorService threadPool = Executors.newCachedThreadPool();
        scheduledThreadPool.scheduleAtFixedRate(new SendTask(addrMultiGroup, port, multicastSocket),
                0, TCL, TimeUnit.SECONDS);
        threadPool.execute(new ReceiveTask(multicastSocket));
        scheduledThreadPool.scheduleAtFixedRate(new CheckExitTask(threadPool, multicastSocket, addrMultiGroup),
                ERT, ERT, TimeUnit.SECONDS);

    }
}