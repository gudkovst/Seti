package application;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Protocol {
    private final String filename;
    private final long filelength;
    private static final int MAX_LEN_FILENAME = 4096;
    private static final int sizeof_int = 4;
    private static final int sizeof_long = 8;
    public static final int lenHeader = sizeof_int + MAX_LEN_FILENAME + sizeof_long;

    public Protocol(byte[] data){
        byte[] namelength = Arrays.copyOfRange(data, 0, sizeof_int);
        int lenName = ByteBuffer.wrap(namelength).getInt();
        filename = new String(Arrays.copyOfRange(data, sizeof_int, sizeof_int + lenName), StandardCharsets.UTF_8);
        filelength = ByteBuffer.wrap(Arrays.copyOfRange(data, sizeof_int + MAX_LEN_FILENAME, data.length)).getLong();
    }

    public String getFilename(){
        return filename;
    }

    public long getLengthFile(){
        return filelength;
    }

}
