package dnss.tools.commons;

public class PropertiesExistException extends RuntimeException {
    public PropertiesExistException() {
        super();
    }

    public PropertiesExistException(String s) {
        super(s);
    }

    public PropertiesExistException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public PropertiesExistException(Throwable throwable) {
        super(throwable);
    }
}
