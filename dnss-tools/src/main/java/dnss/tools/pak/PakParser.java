package dnss.tools.pak;

import dnss.tools.commons.Parser;
import dnss.tools.commons.Producer;
import dnss.tools.commons.ReadStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentHashMap;

public class PakParser implements Parser, Producer<PakFile>, Runnable {
    private static final Logger log = LoggerFactory.getLogger(PakFile.class);
    public static final String HEADER = "EyedentityGames Packing File 0.1";
    public static final long START_POS = 260;
    private static ConcurrentHashMap<String, File> map = new ConcurrentHashMap<String, File>();

    private Pak pak;
    private PakItems items;


    public PakParser(Pak pak, PakItems items) {
        this.pak = pak;
        this.items = items;
    }

    private boolean isValidPak(ReadStream readStream) {
        try {
            String header = readStream.seek(0).readString(HEADER.length());
            return header.equals(HEADER);
        } catch (IOException e) {
            return false;
        }
    }

    public void parse() throws IOException {
        ReadStream readStream = new ReadStream(pak.getLocation());

        if (! isValidPak(readStream)) {
            log.error("Invalid pak file header, aborting parsing " + pak.getLocation().getPath());
            return;
        }

        // gets # of files and start offset
        int numOfFiles = readStream.seek(START_POS).readInt();
        readStream.seek(readStream.readInt());

        FileChannel channel = readStream.getChannel();
        ByteBuffer buf = ByteBuffer.allocateDirect((256 + 4 + 4 + 4 + 4 + 44) * 32); // 10112 bytes/9.875 kb
        int total = 0;
        byte[] path = new byte[256];

        // set order
        buf.order(ByteOrder.LITTLE_ENDIAN);
        while (total < numOfFiles) {
            channel.read(buf);
            buf.flip(); // make it readable up to it slimit
            while (buf.hasRemaining()) {
                PakFile pakFile = new PakFile(pak);
                buf.get(path);
                pakFile.setPakPath(new String(path));
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
        readStream.close();
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