package proxy;

import java.io.IOException;
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
        Proxy proxy = new Proxy("127.0.0.1", Integer.parseInt(args[0]));
        proxy.run();
    }

    private static boolean isCorrect(String param){
        return Pattern.matches("[0-9]+", param);
    }
}
