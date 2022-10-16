import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Protocol {
    private final String filename;
    private final long filelength;
    private final byte[] header;
    private static final int MAX_LEN_FILENAME = 4096;
    private static final int sizeof_int = 4;
    private static final int sizeof_long = 8;
    public static final int lenHeader = sizeof_int + MAX_LEN_FILENAME + sizeof_long;


    public Protocol(String filename) {
        filelength = new File(filename).length();
        this.filename = filename;
        header = makeHeader();
    }

    public byte[] getHeader(){
        return header;
    }

    private byte[] makeHeader(){
        byte[] name = filename.getBytes(StandardCharsets.UTF_8);
        byte[] lenName = ByteBuffer.allocate(sizeof_int).putInt(name.length).array();
        byte[] lenFile = ByteBuffer.allocate(sizeof_long).putLong(filelength).array();
        byte[] header = Arrays.copyOf(lenName, lenHeader);
        System.arraycopy(name, 0, header,  sizeof_int, name.length);
        assert lenFile.length == sizeof_long;
        System.arraycopy(lenFile, 0, header, sizeof_int + MAX_LEN_FILENAME, lenFile.length);
        return header;
    }
}
