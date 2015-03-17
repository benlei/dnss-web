package dnss.tools.pak;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Pak {
    // fields from properties file
    private File location;
    private File destination;
    private boolean flatten;
    private boolean extractDeleted;
    private ArrayList<Pattern> allow;
    private ArrayList<Pattern> ignore;

    // fields for other things
    private String id;

    private int totalFiles;

    private int totalDeletedFiles;

    private int totalSkippedFiles;

    private int totalExtractedFiles;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public File getLocation() {
        return location;
    }

    public void setLocation(File location) {
        this.location = location;
    }

    public File getDestination() {
        return destination;
    }

    public void setDestination(File destination) {
        this.destination = destination;
    }

    public boolean isFlatten() {
        return flatten;
    }

    public void setFlatten(boolean flatten) {
        this.flatten = flatten;
    }

    public boolean isExtractDeleted() {
        return extractDeleted;
    }

    public void setExtractDeleted(boolean extractDeleted) {
        this.extractDeleted = extractDeleted;
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

    // Non-Properties methods
    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public int getTotalDeletedFiles() {
        return totalDeletedFiles;
    }

    public void setTotalDeletedFiles(int totalDeletedFiles) {
        this.totalDeletedFiles = totalDeletedFiles;
    }

    public int getTotalSkippedFiles() {
        return totalSkippedFiles;
    }

    public void setTotalSkippedFiles(int totalSkippedFiles) {
        this.totalSkippedFiles = totalSkippedFiles;
    }

    public int getTotalExtractedFiles() {
        return totalExtractedFiles;
    }

    public void setTotalExtractedFiles(int totalExtractedFiles) {
        this.totalExtractedFiles = totalExtractedFiles;
    }

    public void debug() {

    }

    @Override
    protected Object clone() {
        Pak pak = new Pak();
        pak.setAllow(allow);
        pak.setIgnore(ignore);
        pak.setExtractDeleted(extractDeleted);
        pak.setFlatten(flatten);
        pak.setDestination(destination);
        pak.setLocation(location);
        return pak;
    }
}
