package dnss.tools.pak;

import dnss.tools.commons.ReadStream;
import dnss.tools.commons.Accumulator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class PakParser implements Runnable {
    final static Logger logger = Logger.getLogger(PakParser.class);
    public static final String HEADER = "EyedentityGames Packing File 0.1";
    public static final long START_POS = 260;
    private static final Object LOCK = new Object();
    private static ConcurrentHashMap<String, File> map = new ConcurrentHashMap<String, File>();
    private PakProperties properties;
    private ReadStream readStream;

    public PakParser(PakProperties properties) throws IOException {
        this.properties = properties;
    }


    public PakProperties getProperties() {
        return properties;
    }

    public void setProperties(PakProperties properties) {
        this.properties = properties;
    }

    private boolean isValidPak() throws IOException {
        readStream.seek(0);
        String header = readStream.readString(HEADER.length());
        return header.equals(HEADER);
    }

    public void parse() throws IOException {
        readStream = new ReadStream(properties.getFile());

        if (! isValidPak()) {
            logger.error("Invalid pak file header, aborting parsing " + properties.getFilePath());
            return;
        }

        Accumulator accumulator = properties.getAccumulator();

        // gets # of files and start offset
        readStream.seek(START_POS);
        int numOfFiles = readStream.readInt();
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

            accumulator.accumulate(pakFile);
        }

        properties.setTotalFiles(accumulator.accumulations());
        readStream.close();
    }

    private File getFile(String filePath) {
        File dir = new File(properties.getOutput(), filePath);
        if (! map.containsKey(dir.getPath())) {
            map.put(dir.getPath(), new File(filePath));
        }

        return map.get(dir.getPath());
    }

    public void close() throws IOException {
        readStream.close();
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
