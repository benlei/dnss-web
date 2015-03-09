package dnss.tools.pak;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;


public class Tool {
    final static Logger logger = Logger.getLogger(Tool.class);
    final static String jsonFile = "pak.json";
    private static JSONObject json;
    private static ArrayList<Pattern> globalAllowList = new ArrayList<Pattern>();
    private static ArrayList<Pattern> globalIgnoreList = new ArrayList<Pattern>();
    private static ArrayList<PakProperties> propertiesList = new ArrayList<PakProperties>();
    private static int maxThreads = 10;
    protected static boolean logUnextracted = false;
    protected static Semaphore semaphore;

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

    private static void loadLogUnextracted() {
        if (json.has("logUnextracted")) {
            logUnextracted = json.getBoolean("logUnextracted");
        }
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

            propertiesList.add(properties);
        }
    }

    public static void debugProperties() {
        logger.info("================================================================================");
        logger.info("DNSS Tool - Pak - Properties");
        logger.info("================================================================================");
        logger.info(String.format("%-40s = %d", "maxThreads", maxThreads));
        logger.info(String.format("%-40s = %s", "logUnextracted", String.valueOf(logUnextracted)));
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

    public static void main(String[] args)  {
        loadMaxThreadCount();
        loadLogUnextracted();
        loadGlobalAllowList();
        loadGlobalIgnoreList();
        loadAllPakProperties();

        debugProperties();

        long startTime = System.currentTimeMillis();
        for (PakProperties properties : propertiesList) {
            try {
                File output = properties.getOutput();
                if (! output.exists() && ! output.mkdirs()) {
                    logger.error("Could not create output directory " + output.getPath());
                    continue;
                }

                PakParser pakParser = new PakParser(properties);
                (new Thread(pakParser)).start();
            } catch (IOException e) {
                logger.error(e);
            }
        }

        boolean allDone = false;
        while (! allDone) {
            allDone = true;
            for (PakProperties properties : propertiesList) {
                allDone = allDone && properties.getTotalFiles() > 0 && (properties.getTotalFiles() == properties.getIterFiles());
            }

            if (! allDone) {
                Thread.yield();
            }
        }


        long endTime = System.currentTimeMillis();
        for (PakProperties properties : propertiesList) {
            logger.info("================================================");
            logger.info("Extraction information for " + properties.getFilePath());
            logger.info("================================================");
            logger.info("Total files in pak: " + properties.getTotalFiles());
            logger.info("Total files extracted from pak: " + properties.getExtractedFiles());
        }

        logger.info("Total execution time: " + (endTime-startTime) + "ms");
    }
}
