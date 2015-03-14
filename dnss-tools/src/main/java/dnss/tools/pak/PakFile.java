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
import java.nio.file.Files;

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

    private boolean canLog(String logType) {
        if (DNSS.has("disabledLogs")) {
            Properties prop = DNSS.get("disabledLogs", Properties.class);
            for (int i = 0; i < prop.size(); i++) {
                if (logType.equals(prop.get(i, String.class))) {
                    return false;
                }
            }
        }

        if (properties.has("disabledLogs")) {
            Properties prop = properties.get("disabledLogs", Properties.class);
            for (int i = 0; i < prop.size(); i++) {
                if (logType.equals(prop.get(i, String.class))) {
                    return false;
                }
            }
        }

        return true;
    }


    public void extract() throws IOException, DataFormatException {
        String outputPath;
        if (DNSS.get("flatten", false, Boolean.TYPE) || properties.get("flatten", false, Boolean.TYPE)) {
            outputPath = filePath.substring(filePath.lastIndexOf('\\'));
        } else {
            outputPath = filePath;
        }
        File absoluteFile = new File(properties.get("output", String.class), outputPath);

        if (! properties.get("extractDeleted", false, Boolean.TYPE) && fileSize == 0) {
            if (canLog("deleted")) {
                logger.info("[d] " + absoluteFile.getPath());
            }
            return;
        } else  if (! isExtractAllowed()) {
            if (canLog("ignored")) {
                logger.info("[ ] " + absoluteFile.getPath());
            }
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
            if (absoluteFile.exists()) {
                int i = 1;
                int extPos = outputPath.lastIndexOf('.');
                File outputFile;
                if (extPos == -1) {
                    extPos = outputPath.length();
                }

                String fileWithoutExt = outputPath.substring(0, extPos);
                String fileExt = outputPath.substring(extPos);
                do {
                    outputFile = new File(properties.get("output", String.class), fileWithoutExt + "+" + i + fileExt);
                    i++;
                } while (outputFile.exists());

                Files.move(absoluteFile.toPath(), outputFile.toPath());
            }

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

            if (canLog("allowedExtended")) {
                logger.info("[x] src: " + properties.get("file", String.class) + ", dest: " + absoluteFile.getPath());
            } else if (canLog("allowed")) {
                logger.info("[x] " + absoluteFile.getPath());
            }
        }

        synchronized (properties) {
            properties.set("extractCount", properties.get("extractCount", 0, Integer.TYPE) + 1);
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
