package dnss.tools.pak;

import dnss.tools.commons.ReadStream;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class PakParser implements Runnable {
    private final static Logger logger = Logger.getLogger(PakParser.class);
    public static final String HEADER = "EyedentityGames Packing File 0.1";
    public static final long START_POS = 260;
    private static ConcurrentHashMap<String, File> map = new ConcurrentHashMap<String, File>();
    private PakProperties properties;

    public PakParser(PakProperties properties) {
        this.properties = properties;
    }

    private boolean isValidPak(ReadStream readStream) throws IOException {
        String header = readStream.seek(0).readString(HEADER.length());
        return header.equals(HEADER);
    }

    public void parse() throws IOException {
        ReadStream readStream = new ReadStream(properties.getFile());

        if (! isValidPak(readStream)) {
            logger.error("Invalid pak file header, aborting parsing " + properties.getFilePath());
            return;
        }

        PakFileQueue queue = properties.getQueue();

        // gets # of files and start offset
        int numOfFiles = readStream.seek(START_POS).readInt();
        readStream.seek(readStream.readInt());

        // the skip amount is for after the first iteration
        // It is possible this is better because we can avoid
        // doing an extra skip of 44 bytes for the last iteration
        for (int i = 0, skipAmount = 0; i < numOfFiles; i++, skipAmount = 44) {
            PakFile pakFile = new PakFile(properties);

            // Split up the path after into a HashMap
            pakFile.setFilePath(readStream.skip(skipAmount).readString(256));
            pakFile.setFileSize(readStream.skip(4).readInt()); // potentially useless
            pakFile.setCompressedSize(readStream.readInt());
            pakFile.setStreamOffset(readStream.readInt());

            // setup the file tree for synchronization when extracting/making directories
            pakFile.setFile(getFile(pakFile.getFilePath()));

            queue.enqueue(pakFile);
        }

        properties.setTotalFiles(queue.total());
        readStream.close();
    }

    private File getFile(String filePath) {
        File dir = new File(properties.getOutput(), filePath);
        if (! map.containsKey(dir.getPath())) {
            map.put(dir.getPath(), new File(filePath));
        }

        return map.get(dir.getPath());
    }

    public void run() {
        Semaphore semaphore = properties.getSemaphore();
        try {
            semaphore.acquireUninterruptibly();
            parse();
        } catch (IOException e) {
            logger.error("Could not parse " + properties.getFilePath(), e);
        } finally {
            semaphore.release();
        }
    }
}
