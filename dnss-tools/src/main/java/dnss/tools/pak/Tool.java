package dnss.tools.pak;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;


public class Tool {
    private final static Logger logger = Logger.getLogger(Tool.class);
    private final static String jsonFile = "pak.json";
    private static JSONObject json;
    private static ArrayList<Pattern> globalAllowList = new ArrayList<Pattern>();
    private static ArrayList<Pattern> globalIgnoreList = new ArrayList<Pattern>();
    private static ArrayList<PakProperties> propertiesList = new ArrayList<PakProperties>();
    private static int maxThreads = 10;
    private static Semaphore semaphore;

    static {
        InputStream inputStream = Tool.class.getClassLoader().getResourceAsStream(jsonFile);
        Scanner scanner = new Scanner(inputStream);
        String jsonContents = scanner.useDelimiter("\\Z").next();
        json = new JSONObject(jsonContents);
    }

    private static ArrayList<Pattern> loadPatternList(JSONArray json) {
        ArrayList<Pattern> patternList = new ArrayList<Pattern>();
        for (int i = 0; i < json.length(); i++) {
            String patternString = json.getString(i).replaceAll("\\/", "\\\\\\\\");
            Pattern pattern = Pattern.compile(patternString);
            patternList.add(pattern);
        }

        return patternList;
    }

    private static void loadGlobalAllowList() {
        if (json.has("allow")) {
            globalAllowList = loadPatternList(json.getJSONArray("allow"));
        }
    }

    private static void loadGlobalIgnoreList() {
        if (json.has("ignore")) {
            globalIgnoreList = loadPatternList(json.getJSONArray("ignore"));
        }
    }

    private static void loadMaxThreadCount() {
        if (json.has("maxThreads")) {
            maxThreads = json.getInt("maxThreads");
        }

        semaphore = new Semaphore(maxThreads);
    }

    private static void loadAllPakProperties() {
        JSONArray propertiesListJSON = json.getJSONArray("extract");
        for (int i = 0; i < propertiesListJSON.length(); i++) {
            JSONObject propertiesJSON = propertiesListJSON.getJSONObject(i);
            PakProperties properties = new PakProperties();
            properties.setFile(new File(propertiesJSON.getString("file")));
            if (! properties.getFile().exists()) {
                continue;
            }

            properties.setOutput(new File(propertiesJSON.getString("output")));

            if (propertiesJSON.has("extractDeleted")) {
                properties.setExtractDeleted(propertiesJSON.getBoolean("extractDeleted"));
            } else {
                properties.setExtractDeleted(false);
            }

            if (propertiesJSON.has("allow")) {
                ArrayList<Pattern> list = new ArrayList<Pattern>(globalAllowList);
                list.addAll(loadPatternList(propertiesJSON.getJSONArray("allow")));
                properties.setAllow(list);
            } else {
                properties.setAllow(globalAllowList);
            }

            if (propertiesJSON.has("ignore")) {
                ArrayList<Pattern> list = new ArrayList<Pattern>(globalIgnoreList);
                list.addAll(loadPatternList(propertiesJSON.getJSONArray("ignore")));
                properties.setIgnore(list);
            } else {
                properties.setIgnore(globalIgnoreList);
            }

            properties.setSemaphore(semaphore);
            properties.setMaxThreads(maxThreads);
            properties.setQueue(new PakFileQueue(properties));
            propertiesList.add(properties);
        }
    }

    public static void debugProperties() {
        logger.info("================================================================================");
        logger.info("DNSS Tool - Pak - Properties");
        logger.info("================================================================================");
        logger.info(String.format("%-40s = %d", "maxThreads", maxThreads));
        for (int i = 0; i < globalAllowList.size(); i++) {
            logger.info(String.format("%-40s = %s", "allow[" + i + "]", globalAllowList.get(i).pattern()));
        }

        for (int i = 0; i < globalIgnoreList.size(); i++) {
            logger.info(String.format("%-40s = %s", "ignore[" + i + "]", globalIgnoreList.get(i).pattern()));
        }

        for (int i = 0; i < propertiesList.size(); i++) {
            PakProperties properties = propertiesList.get(i);
            logger.info(String.format("%-40s = %s", "extract[" + i + "].file", properties.getFilePath()));
            logger.info(String.format("%-40s = %s", "extract[" + i + "].output", properties.getOutputPath()));
            for (int j = 0; i < properties.getAllow().size(); j++) {
                logger.info(String.format("%-40s = %s", "extract[" + i + "].allow[" + j + "]",
                        properties.getAllow().get(j).pattern()));
            }

            for (int j = 0; i < properties.getIgnore().size(); j++) {
                logger.info(String.format("%-40s = %s", "extract[" + i + "].ignore[" + j + "]",
                        properties.getIgnore().get(j).pattern()));
            }

            logger.info(String.format("%-40s = %s", "extract[" + i + "].extractDeleted",
                    String.valueOf(properties.canExtractDeleted())));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        loadMaxThreadCount();
        loadGlobalAllowList();
        loadGlobalIgnoreList();
        loadAllPakProperties(); // must be done last

        debugProperties();

        long startTime = System.currentTimeMillis();
        for (PakProperties properties : propertiesList) {
            try {
                PakParser pakParser = new PakParser(properties);
                Thread t = new Thread(pakParser);
                t.setName("dnss.tools.pak");
                t.start();
            } catch (IOException e) {
                logger.error("Could not read " + properties.getFilePath(), e);
            }
        }


        boolean wait = true;
        while (wait) {
            Thread.yield();
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
            wait = false;
            for (Thread t : threadSet) {
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
        for (PakProperties properties : propertiesList) {
            logger.info(properties.getFilePath());
            logger.info("    Total Files Discovered: " + properties.getTotalFiles());
            logger.info("    Total Files Extracted: " + properties.getExtractedFiles());
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
