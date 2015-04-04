package dnss.tools.pak;

import dnss.tools.commons.ReadStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PakFile {
    private static final Logger log = LoggerFactory.getLogger(PakFile.class);

    private String pakPath;
    private File destination;
    private int streamOffset;
    private int fileSize;
    private int compressedSize;

    private static Object LOCK = new Object();

    private Pak pak;

    public PakFile(Pak pak) {
        this.pak = pak;
    }

    public String getPakPath() {
        return pakPath;
    }

    public void setPakPath(String pakPath) {
        this.pakPath = pakPath.substring(0, pakPath.indexOf('\0')).trim();
    }

    public File getDestination() {
        return destination;
    }

    public void setDestination(File destination) {
        this.destination = destination;
    }

    public int getStreamOffset() {
        return streamOffset;
    }

    public void setStreamOffset(int streamOffset) {
        this.streamOffset = streamOffset;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getCompressedSize() {
        return compressedSize;
    }

    public void setCompressedSize(int compressedSize) {
        this.compressedSize = compressedSize;
    }

    public Pak getPak() {
        return pak;
    }

    private boolean canExtract() {
        boolean allowed = false, ignored = false;

        ArrayList<Pattern> allowPatterns = pak.getAllow();
        if (allowPatterns != null && allowPatterns.size() != 0) {
            for (Pattern pattern : allowPatterns) {
                allowed |= pattern.matcher(getPakPath()).find();
            }
        } else {
            allowed = true;
        }

        ArrayList<Pattern> ignorePatterns = pak.getIgnore();
        if (ignorePatterns != null && ignorePatterns.size() != 0) {
            for (Pattern pattern : ignorePatterns) {
                ignored |= pattern.matcher(pakPath).find();
            }
        } else {
            ignored = false;
        }

        return allowed && ! ignored;
    }


    public void extract() throws IOException, DataFormatException {
        String outputPakPath = pakPath;
        File outputDestination = destination;
        if (pak.isFlatten()) {
            outputPakPath = pakPath.substring(pakPath.lastIndexOf('\\'));
            outputDestination = new File(pak.getDestination(), outputPakPath);
        }

        if (! canExtract()) {
            if (System.getProperty("log.ignored").equals("true")) {
                log.warn("[ ] " + outputDestination.getPath());
            }
            synchronized (pak) {
                pak.setTotalSkippedFiles(pak.getTotalSkippedFiles() + 1);
            }
            return;
        } else if (!pak.isExtractDeleted() && fileSize == 0) {
            if (System.getProperty("log.deleted").equals("true") ) {
                log.warn("[d] " + outputDestination.getPath());
            }
            synchronized (pak) {
                pak.setTotalDeletedFiles(pak.getTotalDeletedFiles() + 1);
            }
            return;
        }

        synchronized (LOCK) {
            File dir = outputDestination.getParentFile();
            if (! dir.exists() && ! dir.mkdirs()) {
                log.error("[e] " + outputDestination.getPath() + ", directory could not be created.");
                return;
            }
        }

        RandomAccessFile inStream = new RandomAccessFile(pak.getLocation(), "r");
        byte[] pakContents = new byte[getCompressedSize()];
        inStream.seek(getStreamOffset());
        inStream.readFully(pakContents);
        inStream.close();

        byte[] zData = new byte[8192];
        int read;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Inflater inflater = new Inflater();
        inflater.setInput(pakContents);
        while ((read=inflater.inflate(zData)) != 0) {
            byteArrayOutputStream.write(zData, 0, read);
        }
        inflater.end();

        synchronized (getDestination()) {
            if (outputDestination.exists() && ! pak.isOverwriteExisting()) {
                int i = 1;
                int extPos = outputPakPath.lastIndexOf('.');
                File outputFile;
                if (extPos == -1) {
                    extPos = outputPakPath.length();
                }

                String fileWithoutExt = outputPakPath.substring(0, extPos);
                String fileExt = outputPakPath.substring(extPos);
                do {
                    outputFile = new File(pak.getDestination(), fileWithoutExt + "+" + i + fileExt);
                    i++;
                } while (outputFile.exists());

                Files.move(outputDestination.toPath(), outputFile.toPath());
            }

            FileOutputStream outStream = new FileOutputStream(outputDestination);
            byteArrayOutputStream.writeTo(outStream);
            outStream.close();
        }

        if (System.getProperty("log.extracted").equals("true")) {
            log.info("[x] " + outputDestination.getPath());
        }

        synchronized (pak) {
            pak.setTotalExtractedFiles(pak.getTotalExtractedFiles() + 1);
        }
    }
}
