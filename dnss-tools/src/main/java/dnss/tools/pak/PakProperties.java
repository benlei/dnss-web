package dnss.tools.pak;

import java.io.File;
import java.util.List;

public class PakProperties {
    private File file;
    private File output;
    private List<String> allow;
    private List<String> ignore;
    private boolean extractDeleted;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public List<String> getAllow() {
        return allow;
    }

    public void setAllow(List<String> allow) {
        this.allow = allow;
    }

    public List<String> getIgnore() {
        return ignore;
    }

    public void setIgnore(List<String> ignore) {
        this.ignore = ignore;
    }

    public boolean canExtractDeleted() {
        return extractDeleted;
    }

    public void setExtractDeleted(boolean extractDeleted) {
        this.extractDeleted = extractDeleted;
    }
}
