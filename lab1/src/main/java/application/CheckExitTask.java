package application;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public final class CheckExitTask extends Task {
    private final ExecutorService thisThreadPool;
    private final InetAddress addrMultiGroup;
    private final Scanner scanner;

    CheckExitTask(ExecutorService threadPool, MulticastSocket socket, InetAddress address){
        thisThreadPool = threadPool;
        multicastSocket = socket;
        addrMultiGroup = address;
        scanner = new Scanner(System.in);
    }

    @Override
    public void run(){
        System.out.print("enter x to exit\n");
        if (scanner.hasNext()) {
            if ("x".equals(scanner.next())) {
                scanner.close();
                try {
                    multicastSocket.leaveGroup(addrMultiGroup);
                    thisThreadPool.shutdownNow();
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
