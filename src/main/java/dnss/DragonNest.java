package dnss;

import java.io.IOException;
import java.io.InputStream;

public class DragonNest {
    private static String VERSION_PREFIX = "version ";
    private static int version = -1;

    private static void init() throws IOException {
        try (InputStream in = DragonNest.class.getClassLoader().getResourceAsStream("version.cfg")) {
            byte[] b = new byte[in.available()];
            in.read(b);
            version = Integer.parseInt(new String(b).trim().substring(VERSION_PREFIX.length()));
        }
    }

    public static int getVersion() {
        return version;
    }
}
