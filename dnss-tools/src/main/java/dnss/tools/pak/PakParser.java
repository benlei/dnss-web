package dnss.tools.pak;

import dnss.tools.commons.Parser;
import dnss.tools.commons.Producer;
import dnss.tools.commons.ReadStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class PakParser implements Parser, Producer<PakFile>, Runnable {
    private static final Logger log = LoggerFactory.getLogger(PakFile.class);
    public static final String HEADER = "EyedentityGames Packing File 0.1";
    public static final long START_POS = 260;
    private static ConcurrentHashMap<String, File> map = new ConcurrentHashMap<String, File>();
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private Pak pak;
    private PakItems items;


    public PakParser(Pak pak, PakItems items) {
        this.pak = pak;
        this.items = items;
    }

    public void parse() throws IOException {
        RandomAccessFile stream = new RandomAccessFile(pak.getLocation(), "r");
        FileChannel channel = stream.getChannel();

        // List of buffers/bytes needed
        byte[] headerBytes = new byte[HEADER.length()];
        byte[] path = new byte[256];
        ByteBuffer header = ByteBuffer.wrap(headerBytes);
        ByteBuffer words = ByteBuffer.allocate(8);
        ByteBuffer buf = ByteBuffer.allocateDirect((256 + 4 + 4 + 4 + 4 + 44) * 64); // 19.75 KB

        // configure the buffers
        words.order(LITTLE_ENDIAN);
        buf.order(LITTLE_ENDIAN);

        // Check if the header is okay
        try {
            channel.read(header);
            if (!Arrays.equals(headerBytes, HEADER.getBytes(UTF8))) {
                log.error("Invalid pak file header, aborting parsing " + pak.getLocation().getPath());
                return;
            }
        } catch (IOException e) {
            log.error("Could not read file, aborting parsing " + pak.getLocation().getPath());
            return;
        }

        // Sets where we start
        channel.position(START_POS);
        channel.read(words);

        // gets # of files and start offset
        words.flip(); // read it
        int numOfFiles = words.getInt();
        channel.position(words.getInt());
        int total = 0;

        while (total < numOfFiles) {
            channel.read(buf);
            buf.flip(); // make it readable up to it slimit
            while (buf.hasRemaining()) {
                PakFile pakFile = new PakFile(pak);
                buf.get(path);
                pakFile.setPakPath(new String(path, UTF8));
                buf.position(buf.position() + 4); // skip 4 bytes
                pakFile.setFileSize(buf.getInt());
                pakFile.setCompressedSize(buf.getInt());
                pakFile.setStreamOffset(buf.getInt());
                buf.position(buf.position() + 44); // 44 padding bytes

                total++;
                pakFile.setDestination(resolve(pakFile));
                produce(pakFile);
            }
            buf.clear(); // need to clear buffer for reading in more data
        }

        pak.setTotalFiles(total);
        channel.close();
        stream.close();
    }

    private File resolve(PakFile pakFile) {
        File dir = new File(pak.getDestination(), pakFile.getPakPath());
        if (! map.containsKey(dir.getPath())) {
            map.put(dir.getPath(), dir);
        }

        return map.get(dir.getPath());
    }

    @Override
    public void produce(PakFile item) {
        items.add(item);
    }

    public void run() {
        try {
            Thread.currentThread().setName(pak.getId());
            parse();
        } catch (IOException e) {
            log.error("Could not parse " + pak.getLocation().getPath(), e);
        }
    }
}