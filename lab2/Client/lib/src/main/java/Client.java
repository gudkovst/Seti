import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    private final Socket socket;
    private final String filename;
    private int localPort = 8008;

    public static void main(String[] args) throws IOException {
        if (args.length != 3){
            System.out.println("Inappropriate number of parameters\n Check: [filename] [hostname] [port]");
            return;
        }
        Client client = new Client(args[0], args[1], Integer.parseInt(args[2]));
        System.out.println(client.send());
    }

    public Client(String filename, String host, int port) throws IOException {
        socket = new Socket(host, port, InetAddress.getLocalHost(), localPort);
        this.filename = filename;
    }

    public String send() throws IOException {
        Protocol pack = new Protocol(filename);
        byte[] data = pack.getHeader();
        socket.getOutputStream().write(data);
        FileInputStream fileInputStream = new FileInputStream(filename);
        socket.getOutputStream().write(fileInputStream.readAllBytes());
        fileInputStream.close();
        String mess = new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        socket.close();
        return mess;
    }
}
