package dnss.tools.pak;

import dnss.tools.commons.ReadStream;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


public class PakFile implements Runnable {
    final static Logger logger = Logger.getLogger(PakFile.class);

    private PakProperties properties;

    private int streamOffset;

    private int compressedSize;

    private int fileSize;

    private String filePath;

    private File file;

    private static Object LOCK = new Object();

    public PakFile(PakProperties properties) {
        this.properties = properties;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath =  filePath.substring(0, filePath.indexOf('\0')).trim();
    }

    public int getStreamOffset() {
        return streamOffset;
    }

    public void setStreamOffset(int streamOffset) {
        this.streamOffset = streamOffset;
    }

    public int getCompressedSize() {
        return compressedSize;
    }

    public void setCompressedSize(int compressedSize) {
        this.compressedSize = compressedSize;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public PakProperties getProperties() {
        return properties;
    }

    public void setProperties(PakProperties properties) {
        this.properties = properties;
    }

    public boolean isExtractAllowed() {
        boolean allowed = false, ignored = false;
        if (properties.getAllow().size() == 0) {
            allowed = true;
        } else {
            for (Pattern pattern : properties.getAllow()) {
                allowed |= pattern.matcher(filePath).find();
            }
        }

        if (properties.getIgnore().size() == 0) {
            ignored = false;
        } else {
            for (Pattern pattern : properties.getAllow()) {
                ignored |= pattern.matcher(filePath).find();
            }
        }

        return allowed && ! ignored;
    }


    public void extract() throws IOException, DataFormatException {
        File absoluteFile = new File(properties.getOutput(), filePath);

        if (! properties.canExtractDeleted() && fileSize == 0) {
            logger.info("[d] " + absoluteFile.getPath());
            return;
        } else  if (! isExtractAllowed()) {
            logger.info("[ ] " + absoluteFile.getPath());
            return;
        }

        synchronized (file) {
            ReadStream readStream = new ReadStream(properties.getFile());

            synchronized (LOCK) {
                File dir = absoluteFile.getParentFile();
                if (! dir.exists() && ! dir.mkdirs()) {
                    logger.error("Could not create directory " + dir.getPath());
                }
            }

            readStream.seek(streamOffset);

            byte[] pakContents = new byte[compressedSize];
            readStream.readFully(pakContents);
            readStream.close();

            byte[] inflatedPakContents = new byte[8192];
            FileOutputStream fileOutputStream = new FileOutputStream(absoluteFile);
            Inflater inflater = new Inflater();
            inflater.setInput(pakContents);
            while (! inflater.finished()) {
                int inflatedSize = inflater.inflate(inflatedPakContents);
                fileOutputStream.write(inflatedPakContents, 0, inflatedSize);
            }

            inflater.end();
            fileOutputStream.close();

            logger.info("[x] " + absoluteFile.getPath());
            properties.increaseExtractedFilesCount();
        }
    }

    public void run() {
        PakFileQueue queue = properties.getQueue();
        Semaphore semaphore = properties.getSemaphore();
        try {
            semaphore.acquireUninterruptibly();
            extract();
        } catch(IOException e) {
            logger.error("Could not extract " + filePath + " from " + properties.getFilePath(), e);
        } catch (DataFormatException e) {
            logger.error("Could not extract zipped content " + filePath + " from " +  properties.getFilePath(), e);
        } finally {
            queue.dequeue();
            semaphore.release();
        }
    }
}
