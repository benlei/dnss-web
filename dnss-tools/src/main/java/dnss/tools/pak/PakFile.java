package dnss.tools.pak;

import dnss.tools.commons.DNSS;
import dnss.tools.commons.Properties;
import dnss.tools.commons.ReadStream;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PakFile implements Runnable {
    private final static Logger logger = Logger.getLogger(PakFile.class);

    private Properties properties;

    private int streamOffset;

    private int compressedSize;

    private int fileSize;

    private String filePath;

    private File file;

    private static Object LOCK = new Object();

    public PakFile(Properties properties) {
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

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public boolean isExtractAllowed() {
        boolean allowed = false, ignored = false;

        // global check first
        if (DNSS.has("allowPatterns")) {
            ArrayList<Pattern> allowPatterns = DNSS.get("allowPatterns", ArrayList.class);
            for (Pattern pattern : allowPatterns) {
                allowed |= pattern.matcher(filePath).find();
            }
        }

        if (properties.has("allowPatterns")) {
            ArrayList<Pattern> allowPatterns = properties.get("allowPatterns", ArrayList.class);
            for (Pattern pattern : allowPatterns) {
                allowed |= pattern.matcher(filePath).find();
            }
        }

        if (! DNSS.has("allowPatterns") && ! properties.has("allowPatterns")) {
            allowed = true;
        }

        if (DNSS.has("ignorePatterns")) {
            ArrayList<Pattern> ignorePatterns = DNSS.get("ignorePatterns", ArrayList.class);
            for (Pattern pattern : ignorePatterns) {
                ignored |= pattern.matcher(filePath).find();
            }
        }

        if (properties.has("ignorePatterns")) {
            ArrayList<Pattern> ignorePatterns = properties.get("ignorePatterns", ArrayList.class);
            for (Pattern pattern : ignorePatterns) {
                ignored |= pattern.matcher(filePath).find();
            }
        }

        if (! DNSS.has("ignorePatterns") && ! properties.has("ignorePatterns")) {
            ignored = false;
        }

        return allowed && ! ignored;
    }


    public void extract() throws IOException, DataFormatException {
        File absoluteFile = new File(properties.get("output", String.class), filePath);

        if (! properties.get("extractDeleted", Boolean.TYPE) && fileSize == 0) {
            logger.info("[d] " + absoluteFile.getPath());
            return;
        } else  if (! isExtractAllowed()) {
            logger.info("[ ] " + absoluteFile.getPath());
            return;
        }

        ReadStream readStream = new ReadStream(properties.get("file", String.class));

        synchronized (LOCK) {
            File dir = absoluteFile.getParentFile();
            if (! dir.exists() && ! dir.mkdirs()) {
                logger.error("Could not create directory " + dir.getPath());
            }
        }

        byte[] pakContents = new byte[compressedSize];
        readStream.seek(streamOffset).readFully(pakContents);
        readStream.close();

        synchronized (file) {
            int i = 1;
            File outputFile = absoluteFile;
            while (outputFile.exists()) {
                int extPos = filePath.lastIndexOf('.');
                if (extPos == -1) {
                    extPos = filePath.length();
                }

                String newPath = filePath.substring(0, extPos) + i++ + filePath.substring(extPos);
                outputFile = new File(properties.get("output", String.class), newPath);
            }

            byte[] inflatedPakContents = new byte[8192];
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            Inflater inflater = new Inflater();
            inflater.setInput(pakContents);
            while (! inflater.finished()) {
                int inflatedSize = inflater.inflate(inflatedPakContents);
                fileOutputStream.write(inflatedPakContents, 0, inflatedSize);
            }

            inflater.end();
            fileOutputStream.close();
            logger.info("[x] " + outputFile.getPath());
        }

        synchronized (properties) {
            properties.set("extractCount", properties.get("extractCount", Integer.TYPE) + 1);
        }

    }

    public void run() {
        PakAccumulator accumulator = properties.get("accumulator", PakAccumulator.class);
        Semaphore semaphore = DNSS.get("semaphore", Semaphore.class);
        try {
            semaphore.acquireUninterruptibly();
            extract();
        } catch(IOException e) {
            logger.error("Could not extract " + filePath + " from " + properties.get("file", String.class), e);
        } catch (DataFormatException e) {
            logger.error("Could not extract zipped content " + filePath + " from " +  properties.get("file", String.class), e);
        } finally {
            accumulator.dissipate();
            semaphore.release();
        }
    }
}
