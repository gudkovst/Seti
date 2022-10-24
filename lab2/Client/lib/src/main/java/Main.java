import java.io.IOException;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 3){
            System.err.println("Inappropriate number of parameters\n Check: [filename] [hostname] [port]");
            return;
        }
        if (!isCorrect(args[2])){
            System.err.println("Incorrect parameter [port]. Must be integer");
            return;
        }
        Client client = new Client(args[0], args[1], Integer.parseInt(args[2]));
        System.out.println(client.send());
    }

    private static boolean isCorrect(String param){
        return Pattern.matches("[0-9]+", param);
    }
}
