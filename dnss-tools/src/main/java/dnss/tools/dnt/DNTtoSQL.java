package dnss.tools.dnt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DNTtoSQL {
    private static final Logger log = LoggerFactory.getLogger(DNTtoSQL.class);

    private final static String props = "dnt.properties";

    public static void main(String[] args) throws Exception {
        InputStream is;
        if (0 < args.length) {
            is = new FileInputStream(args[0]);
        } else {
            is = DNTtoSQL.class.getClassLoader().getResourceAsStream(props);
        }

        Properties properties = new Properties();
        byte[] propertiesBytes = new byte[is.available()];
        is.read(propertiesBytes);
        properties.load(new StringReader((new String(propertiesBytes)).replace("\\","\\\\")));
        is.close();

        // set the system properties
        int maxThreads = Integer.valueOf(properties.getProperty("system.max.threads", System.getProperty("max.threads", "1")));
        System.setProperty("max.threads", String.valueOf(maxThreads));

        HashMap<String, DNT> dntMap = new HashMap<String, DNT>();
        for (String name : properties.stringPropertyNames()) {
            if (! name.startsWith("dnt.")) {
                continue;
            }

            String dntId = name.substring(4);
            dntId = dntId.substring(0, dntId.indexOf('.'));
            if (! dntMap.containsKey(dntId)) {
                String prefix = "dnt." + dntId + ".";

                DNT dnt = new DNT();
                dnt.setId(dntId);
                dnt.setLocation(new File(properties.getProperty(prefix + "location")));
                dnt.setDestination(new File(properties.getProperty(prefix + "destination")));
                dntMap.put(dntId, dnt);
            }
        }

        ExecutorService service = Executors.newFixedThreadPool(maxThreads);
        for (DNT dnt : dntMap.values()) {
            DNTParser parser = new DNTParser(dnt);
            service.submit(parser);
        }

        service.shutdown();
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // something went wrong
        }
    }
}
