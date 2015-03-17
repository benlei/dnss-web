package dnss.tools.pak;

import dnss.tools.commons.Parser;
import dnss.tools.commons.Producer;
import dnss.tools.commons.ReadStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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

        // the skip amount is for after the first iteration
        // It is possible this is better because we can avoid
        // doing an extra skip of 44 bytes for the last iteration
        int i, skipAmount;
        for (i = 0, skipAmount = 0; i < numOfFiles; i++, skipAmount = 44) {
            PakFile pakFile = new PakFile(pak);

            // Split up the path after into a HashMap
            pakFile.setPakPath(readStream.skip(skipAmount).readString(256));
            pakFile.setFileSize(readStream.skip(4).readInt()); // potentially useless
            pakFile.setCompressedSize(readStream.readInt());
            pakFile.setStreamOffset(readStream.readInt());

            // setup the file tree for synchronization when extracting/making directories
            pakFile.setDestination(resolve(pakFile));

            produce(pakFile);
        }

        pak.setTotalFiles(i);
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
//            logger.error("Could not parse " + pak.getLocation().getPath(), e);
        }
    }
}
