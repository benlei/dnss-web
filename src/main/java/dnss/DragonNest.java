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
            String cfg = new String(b);
            int i = 0, j = 0;
            while (! Character.isDigit(cfg.charAt(i))) {
                i++;
                j++;
            }

            while (Character.isDigit(cfg.charAt(j))) {
                j++;
            }

            version = Integer.parseInt(cfg.substring(i, j));
        }

//        version = (int)(System.currentTimeMillis() / 1000);
    }

    public static int getVersion() {
        return version;
    }
}
