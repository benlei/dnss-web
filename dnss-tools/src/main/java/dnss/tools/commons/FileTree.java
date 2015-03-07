package dnss.tools.commons;

import java.io.File;

public class FileTree {
    private FileTree parent;
    private File file;

    public FileTree(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public FileTree getParent() {
        return parent;
    }

    public void setParent(FileTree parent) {
        this.parent = parent;
    }

    public FileTree createChild(File file) {
        FileTree fileTree = new FileTree(file);
        fileTree.setParent(this);
        return fileTree;
    }
}
