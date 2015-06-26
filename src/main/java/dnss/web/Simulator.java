package dnss.web;

import org.apache.commons.io.IOUtils;
import org.ini4j.IniPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;
import java.util.prefs.Preferences;

public class Simulator {
    private final static String INI_FILE = "dnss.ini";
    private static int version;

    static {
        org.ini4j.Config.getGlobal().setEscape(false);
    }

    public static void init() throws Exception {
        String home = System.getProperty("user.home", System.getenv("HOME"));

        FileInputStream in = new FileInputStream(new File(home, INI_FILE));
        Preferences ini = new IniPreferences(in);
        in.close();

//        String web = properties.getProperty("http.version");
//        URL url = new URL(web);
//
//        version = Integer.parseInt(IOUtils.toString(url).substring("version".length()).trim());

    }
}
