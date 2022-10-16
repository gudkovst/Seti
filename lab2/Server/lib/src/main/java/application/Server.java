package application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable {
    private static Logger logger;
    private static final String messSuccess = "File received successfully!";
    private static final String messError = "File received with an error!";
    private final ServerSocket serverSocket;
    private int count = 0;
    private long perSizeFile = 0;
    private long sizeFile = 0;
    private static final int TIME_MEAS = 3; //время между замерами
    private final String delimiter;
    private final String path;

    public static void main(String[] args) throws IOException {
        if (args.length != 1){
            System.err.println("Inappropriate number of parameters\n Check: [port]");
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
                    logger.log(Level.INFO, "end of work server");
                    System.exit(0);
                }
            }
        }
    }

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        if (System.getProperty("os.name").toLowerCase().contains("windows"))
            delimiter = "\\";
        else delimiter = "/";
        path = "." + delimiter + "uploads" + delimiter;
        InputStream stream = this.getClass().getResourceAsStream("/logger.properties");
        Properties properties = new Properties();
        properties.load(stream);
        logger = Logger.getLogger(this.getClass().getName());
        logger.log(Level.INFO, "start of work server");
    }

    @Override
    public void run() {
        ExecutorService clientPool = Executors.newCachedThreadPool();
        while (!Thread.currentThread().isInterrupted()){
            try {
                Socket newClient = serverSocket.accept();
                logger.log(Level.INFO, "new client " + newClient.getInetAddress());
                clientPool.execute(() -> {
                    try {
                        downloadFile(newClient);
                        logger.log(Level.INFO, "client " + newClient.getInetAddress() + " serviced");
                        newClient.close();
                    } catch (IOException e) {
                        logger.log(Level.FINE, e.getMessage());
                    }
                });
            } catch (IOException e) {
                logger.log(Level.FINE, e.getMessage());
            }
        }
        clientPool.shutdownNow();
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.log(Level.FINE, e.getMessage());
        }
    }

    private void downloadFile(Socket socket) throws IOException {
        byte[] header = socket.getInputStream().readNBytes(Protocol.lenHeader);
        Protocol data = new Protocol(header);
        File file = createFile(data.getFilename());
        logger.log(Level.INFO, "create file " + file.getName());
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
        scheduledThreadPool.scheduleAtFixedRate(() -> {
                    try {
                        calcSpeed();
                    } catch (InterruptedException e) {
                        logger.log(Level.FINE, e.getMessage());
                    }
                },
                TIME_MEAS, TIME_MEAS, TimeUnit.SECONDS);
        FileOutputStream outputStream = new FileOutputStream(file);
        long lenFile = data.getLengthFile();
        long beginTime = System.currentTimeMillis();
        while (lenFile > 0) {
            int portion = (int) lenFile;
            sizeFile += portion;
            outputStream.write(socket.getInputStream().readNBytes(portion));
            lenFile -= portion;
        }
        scheduledThreadPool.shutdownNow();
        if (count == 0) {
            long curTime = System.currentTimeMillis();
            if (curTime - beginTime == 0) {
                logger.log(Level.INFO, "download very-very fast");
            }
            else {
                double speed = (double) data.getLengthFile() / (curTime - beginTime);
                String instSpeed = "instantaneous speed = " + String.format("%.3f", speed) + " byte / sec";
                logger.log(Level.INFO, instSpeed);
                String averSpeed = "average speed = " + String.format("%.3f", speed) + " byte / sec";
                logger.log(Level.INFO, averSpeed);
            }
        }
        outputStream.close();
        String message = (data.getLengthFile() == file.length()) ? messSuccess : messError;
        socket.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
    }

    private File createFile(String clientFilename) throws IOException {
        String filename = path + clientFilename.substring(clientFilename.lastIndexOf(delimiter) + 1);
        String fileExtens = filename.substring(filename.lastIndexOf("."));
        String fileName = filename.substring(0, filename.lastIndexOf("."));
        File file = new File(filename);
        for (int i = 1; !file.createNewFile(); i++){
            file = new File(fileName + i + fileExtens);
        }
        return file;
    }

    private void calcSpeed() throws InterruptedException {
        if (Thread.currentThread().isInterrupted())
            return;
        long curSizeFile = sizeFile;
        count++;
        String instSpeed = "instantaneous speed = " +
                String.format("%.3f", (double)(curSizeFile - perSizeFile) / TIME_MEAS) + " byte / sec";
        logger.log(Level.INFO, instSpeed);
        String averSpeed = "average speed = " +
                String.format("%.3f", (double)curSizeFile / (count * TIME_MEAS)) + " byte / sec";
        logger.log(Level.INFO, averSpeed);
        perSizeFile = curSizeFile;
    }
}
