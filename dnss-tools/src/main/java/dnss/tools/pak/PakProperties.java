package dnss.tools.pak;

import dnss.tools.commons.Accumulator;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

public class PakProperties {
    private File file;
    private File output;
    private ArrayList<Pattern> allow;
    private ArrayList<Pattern> ignore;
    private boolean extractDeleted;
    private int totalFiles;
    private int extractedFiles;
    private Semaphore semaphore;
    private Accumulator accumulator;
    private int maxThreads;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFilePath() {
        return file.getPath();
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public String getOutputPath() {
        return output.getPath();
    }

    public ArrayList<Pattern> getAllow() {
        return allow;
    }

    public void setAllow(ArrayList<Pattern> allow) {
        this.allow = allow;
    }

    public ArrayList<Pattern> getIgnore() {
        return ignore;
    }

    public void setIgnore(ArrayList<Pattern> ignore) {
        this.ignore = ignore;
    }

    public boolean canExtractDeleted() {
        return extractDeleted;
    }

    public void setExtractDeleted(boolean extractDeleted) {
        this.extractDeleted = extractDeleted;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public int getExtractedFiles() {
        return extractedFiles;
    }

    public synchronized void increaseExtractedFilesCount() {
        ++extractedFiles;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public Accumulator getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(Accumulator accumulator) {
        this.accumulator = accumulator;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }
}
