package dnss.tools.pak;

import dnss.tools.commons.ReadStream;
import dnss.tools.commons.FileTree;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class PakParser {
    public static final String HEADER = "EyedentityGames Packing File 0.1";
    public static final long START_POS = 260;
    private static ConcurrentHashMap<String, FileTree> hashMap = new ConcurrentHashMap<String, FileTree>();
    private PakProperties properties;
    private ReadStream readStream;

    public PakParser(PakProperties properties) throws IOException {
        this.properties = properties;
        readStream = new ReadStream(properties.getFile());
    }

    private boolean isValidPak() throws IOException {
        readStream.seek(0);
        String header = readStream.readString(HEADER.length());
        return header.equals(HEADER);
    }

    public List<PakFile> parse() throws IOException {
        if (! isValidPak()) {
            return null;
        }

        List<PakFile> pakFiles = new ArrayList<PakFile>();

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

            // setup the file tree for synchronization when extracting/making directories
            File file = new File(pakFile.getFilePath());
            FileTree fileTree = generateFileTree(file);
            pakFile.setFileTree(fileTree);

            pakFile.setFileSize(readStream.skip(4).readInt()); // potentially useless
            pakFile.setCompressedSize(readStream.readInt());
            pakFile.setStreamOffset(readStream.readInt());
            pakFiles.add(pakFile);
        }

        return pakFiles;
    }

    private FileTree generateFileTree(File file) {
        Stack<File> stack = new Stack<File>();
        File absoluteFile;
        FileTree fileTree = null;

        while (! file.getPath().equals(File.separator)) {
            absoluteFile = new File(properties.getOutput(), file.getPath());
            if (hashMap.containsKey(absoluteFile.getPath())) {
                fileTree = hashMap.get(absoluteFile.getPath());
                break;
            }

            stack.push(file);
            file = file.getParentFile();
        }

        if (fileTree == null) {
            file = stack.pop();
            fileTree = new FileTree(file);
            absoluteFile = new File(properties.getOutput(), file.getPath());
            hashMap.put(absoluteFile.getPath(), fileTree);
        }

        while (! stack.empty()) {
            file = stack.pop();
            absoluteFile = new File(properties.getOutput(), file.getPath());
            fileTree = fileTree.createChild(file);
            hashMap.put(absoluteFile.getPath(), fileTree);
        }

        return fileTree;
    }

    public void close() throws IOException {
        readStream.close();
    }
}
