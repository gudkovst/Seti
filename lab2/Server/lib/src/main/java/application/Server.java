package application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Server implements Runnable {
    public static Logger logger;
    private static final String messSuccess = "File received successfully!";
    private static final String messError = "File received with an error!";
    private final ServerSocket serverSocket;
    private int count = 0;
    private long perSizeFile = 0;
    private long sizeFile = 0;
    private static final int TIME_MEAS = 3; //время между замерами
    private String delimiter = "/";
    private final String path;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        path = "." + delimiter + "uploads" + delimiter;
        InputStream stream = this.getClass().getResourceAsStream("/logger.properties");
        Properties properties = new Properties();
        properties.load(stream);
        logger = Logger.getLogger(this.getClass().getName());
        logger.info("start of work server");
    }

    @Override
    public void run() {
        ExecutorService clientPool = Executors.newCachedThreadPool();
        while (!Thread.currentThread().isInterrupted()){
            try {
                Socket newClient = serverSocket.accept();
                logger.info("new client " + newClient.getInetAddress());
                clientPool.execute(() -> {
                    try {
                        downloadFile(newClient);
                        logger.info("client " + newClient.getInetAddress() + " serviced");
                        newClient.close();
                    } catch (IOException e) {
                        logger.fine(e.getMessage());
                    }
                });
            } catch (IOException e) {
                logger.fine(e.getMessage());
            }
        }
        clientPool.shutdownNow();
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.fine(e.getMessage());
        }
    }

    private void downloadFile(Socket socket) throws IOException {
        byte[] header = socket.getInputStream().readNBytes(Protocol.lenHeader);
        Protocol data = new Protocol(header);
        File file = createFile(data.getFilename());
        logger.info("create file " + file.getName());
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
        scheduledThreadPool.scheduleAtFixedRate(() -> {
                    try {
                        calcSpeed();
                    } catch (InterruptedException e) {
                        logger.fine(e.getMessage());
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
                logger.info("download very-very fast");
            }
            else {
                double speed = (double) data.getLengthFile() / (curTime - beginTime);
                String instSpeed = "instantaneous speed = " + String.format("%.3f", speed) + " byte / sec";
                logger.info(instSpeed);
                String averSpeed = "average speed = " + String.format("%.3f", speed) + " byte / sec";
                logger.info(averSpeed);
            }
        }
        outputStream.close();
        String message = (data.getLengthFile() == file.length()) ? messSuccess : messError;
        socket.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
    }

    private File createFile(String clientFilename) throws IOException {
        if (clientFilename.contains("\\")){ //filename cannot contain / and \
            delimiter = "\\";
        }
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
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        long curSizeFile = sizeFile;
        count++;
        String instSpeed = "instantaneous speed = " +
                String.format("%.3f", (double)(curSizeFile - perSizeFile) / TIME_MEAS) + " byte / sec";
        logger.info(instSpeed);
        String averSpeed = "average speed = " +
                String.format("%.3f", (double)curSizeFile / (count * TIME_MEAS)) + " byte / sec";
        logger.info(averSpeed);
        perSizeFile = curSizeFile;
    }
}
