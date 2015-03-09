package dnss.tools.pak;

import dnss.tools.commons.FileTree;
import dnss.tools.commons.ReadStream;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private FileTree fileTree;

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

    public FileTree getFileTree() {
        return fileTree;
    }

    public void setFileTree(FileTree fileTree) {
        this.fileTree = fileTree;
    }

    public PakProperties getProperties() {
        return properties;
    }

    public void setProperties(PakProperties properties) {
        this.properties = properties;
    }

    // make iterative, not recursive.
    private void createDirectories(FileTree fileTree) {
        if (fileTree == null) {
            return;
        }

        File fileLock = fileTree.getFile();
        File dir = new File(properties.getOutput(), fileLock.getPath());
        if (! dir.exists()) {
            createDirectories(fileTree.getParent());
            synchronized (fileLock) {
                if (! dir.mkdir()) { // note: the output dir must be fully made outside of this method
                    logger.error("Could not create directories for " + dir.getPath());
                }
            }
        }

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
        File pakFile = fileTree.getFile();
        File absoluteFile = new File(properties.getOutput(), pakFile.getPath());

        if (! properties.canExtractDeleted() && fileSize == 0) {
            logger.info("[d] " + absoluteFile.getPath());
            properties.increaseIterFiles();
            return;
        } else  if (! isExtractAllowed()) {
            if (Tool.logUnextracted) {
                logger.info("[ ] " + absoluteFile.getPath());
            }

            properties.increaseIterFiles();
            return;
        }

        synchronized (pakFile) {
            ReadStream readStream = new ReadStream(properties.getFile());
            createDirectories(fileTree.getParent());

            readStream.seek(streamOffset);

            byte[] pakContents = new byte[compressedSize];
            readStream.readFully(pakContents);
            readStream.close();

            byte[] inflatedPakContents = new byte[1024];
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
            properties.increaseIterFiles();
        }
    }

    public void run() {
        try {
            Tool.semaphore.acquireUninterruptibly();
            extract();
        } catch(IOException e) {
            logger.error("Could not extract " + filePath + " from " + properties.getFilePath(), e);
        } catch (DataFormatException e) {
            logger.error("Could not extract zipped content " + filePath + " from " +  properties.getFilePath(), e);
        } finally {
            Tool.semaphore.release();
        }
    }
}
