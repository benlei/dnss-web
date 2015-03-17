package dnss.tools.dnt;

import java.io.File;

public class DNT {
    private String id;
    private File location;
    private File destination;

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
}
