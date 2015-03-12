package dnss.tools.dnt;

import java.io.File;

public class DNTProperties {
    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFilePath() {
        return file.getPath();
    }
}
