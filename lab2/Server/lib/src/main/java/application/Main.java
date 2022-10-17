package application;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 1){
            System.err.println("Inappropriate number of parameters\n Check: [port]");
            return;
        }
        if (!isCorrect(args[0])){
            System.err.println("Incorrect parameter. Must be integer");
            return;
        }
        Thread server = new Thread(new Server(Integer.parseInt(args[0])));
        server.start();
        Scanner scanner = new Scanner(System.in);
        System.out.print("enter x to exit server\n");
        while (true) {
            if (scanner.hasNext()) {
                if ("x".equals(scanner.next())) {
                    scanner.close();
                    server.interrupt();
                    Server.logger.info("end of work server");
                    System.exit(0);
                }
            }
        }
    }

    private static boolean isCorrect(String param){
        return Pattern.matches("[0-9]+", param);
    }
}
