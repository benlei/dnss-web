package dnss.tools.pak;

import dnss.tools.commons.FileTree;
import dnss.tools.commons.ReadStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


public class PakFile {
    public static final int BUFSIZ = 10240; // 10K

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

    public void createDirectories(FileTree fileTree) {
        if (fileTree == null) {
            return;
        }

        createDirectories(fileTree.getParent());
        File pakFile = fileTree.getFile();
        synchronized (pakFile) {
            File dir = new File(properties.getOutput(), pakFile.getPath());
            if (! dir.exists() && ! dir.mkdir()) { // note: the output dir must be fully made outside of this method
                // something went wrong...
            }
        }
    }

    public void extract() throws IOException, DataFormatException {
        if (! properties.canExtractDeleted() && fileSize == 0) {
            return;
        }

        synchronized (fileTree.getFile()) {
            ReadStream readStream = new ReadStream(properties.getFile());
            File file = new File(properties.getOutput(), fileTree.getFile().getPath());
            createDirectories(fileTree.getParent());

            readStream.seek(streamOffset);

            byte[] pakContents = new byte[compressedSize];
            readStream.readFully(pakContents);
            readStream.close();

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            Inflater inflater = new Inflater();
            inflater.setInput(pakContents);
            while (!inflater.finished()) {
                byte[] inflatedPakContents = new byte[BUFSIZ];
                int inflatedSize = inflater.inflate(inflatedPakContents);
                fileOutputStream.write(inflatedPakContents, 0, inflatedSize);
            }

            inflater.end();
            fileOutputStream.close();
        }
    }
}
