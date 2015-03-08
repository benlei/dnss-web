package dnss.tools.pak;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class PakProperties {
    private File file;
    private File output;
    private HashMap<String, Pattern> allow;
    private HashMap<String, Pattern> ignore;
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

    public HashMap<String, Pattern> getAllow() {
        return allow;
    }

    public void setAllow(HashMap<String, Pattern> allow) {
        this.allow = allow;
    }

    public HashMap<String, Pattern> getIgnore() {
        return ignore;
    }

    public void setIgnore(HashMap<String, Pattern> ignore) {
        this.ignore = ignore;
    }

    public boolean canExtractDeleted() {
        return extractDeleted;
    }

    public void setExtractDeleted(boolean extractDeleted) {
        this.extractDeleted = extractDeleted;
    }
}
