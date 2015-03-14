package dnss.tools.pak;

import dnss.tools.commons.DNSS;
import dnss.tools.commons.JSONPropertiesParser;
import dnss.tools.commons.Properties;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;


public class Tool {
    private final static Logger logger = Logger.getLogger(Tool.class);
    private final static String jsonFile = "pak.json";

    private static void loadPatternList(String property, String propertyPattern) {
        ArrayList<Pattern> patternList = new ArrayList<Pattern>();
        Properties properties = DNSS.get(property, Properties.class);
        for (int i = 0; i < properties.size(); i++) {
            String patternString = properties.get(i, String.class).replaceAll("\\/", "\\\\\\\\");
            Pattern pattern = Pattern.compile(patternString);
            patternList.add(pattern);
        }

        if (! patternList.isEmpty()) {
            DNSS.set(propertyPattern, patternList);
        }
    }

    private static void loadGlobalAllowList() {
        if (DNSS.has("allow")) {
            loadPatternList("allow", "allowPatterns");
        }
    }

    private static void loadGlobalIgnoreList() {
        if (DNSS.has("ignore")) {
            loadPatternList("ignore", "ignorePatterns");
        }
    }

    private static void loadMaxThreadCount() {
        int defaultVal = 1;
        if (DNSS.has("maxThreads")) {
            defaultVal = DNSS.get("maxThreads", Integer.TYPE);
        }

        DNSS.set("semaphore", new Semaphore(defaultVal));
    }

    private static void loadAllPakProperties() {
        Properties paks = DNSS.get("paks", Properties.class);
        for (int i = 0; i < paks.size(); i++) {
            Properties pak = paks.get(i, Properties.class);
            if (pak.has("allow")) {
                loadPatternList("paks."+i+".allow", "paks."+i+".allowPatterns");
            }

            if (pak.has("ignore")) {
                loadPatternList("paks."+i+".ignore", "paks."+i+".ignorePatterns");
            }

            pak.set("accumulator", new PakAccumulator());
            pak.set("extractCount", 0);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        InputStream inputStream;
        if (args.length == 1) {
            inputStream = new FileInputStream(args[1]);
        } else {
            inputStream = Tool.class.getClassLoader().getResourceAsStream(jsonFile);
        }
        JSONPropertiesParser.parse(inputStream);

        loadMaxThreadCount();
        loadGlobalAllowList();
        loadGlobalIgnoreList();
        loadAllPakProperties();

        long startTime = System.currentTimeMillis();
        Properties prop = DNSS.get("paks", Properties.class);
        for (int i = 0; i < prop.size(); i++) {
            PakParser pakParser = new PakParser(prop.get(i, Properties.class));
            Thread t = new Thread(pakParser);
            t.setName("dnss.tools.pak");
            t.start();
        }


        boolean wait = true;
        while (wait) {
            Thread.yield();
            wait = false;
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                if (t.getName().equals("dnss.tools.pak")) {
                    wait = true;
                    break;
                }
            }
        }

        long endTime = System.currentTimeMillis();

        logger.info("================================================================================");
        logger.info("Extraction Summary");
        logger.info("================================================================================");
        for (int i = 0; i < prop.size(); i++) {
            Properties p = prop.get(i, Properties.class);
            logger.info(p.get("file", String.class));
            logger.info("    Total Files Discovered: " + p.get("totalFiles", Integer.TYPE));
            logger.info("    Total Files Extracted: " + p.get("extractCount", Integer.TYPE));
        }

        long timeInMS  = endTime - startTime;
        long timeInS   = timeInMS / 1000;
        long timeInM_M = timeInS / 60;
        long timeInM_S = timeInS - (timeInM_M * 60);
        long timeInH_H = timeInM_M / 60;
        long timeInH_M = timeInM_M - (timeInH_H * 60);

        logger.info("Total execution time: " + timeInH_H + " hr, " + timeInH_M + " min, and " + timeInM_S + " s, or");
        logger.info("                      " + timeInM_M + " min and " + timeInM_S + " s, or");
        logger.info("                      " + timeInS + " s, or");
        logger.info("                      " + timeInMS + " ms");

    }
}
