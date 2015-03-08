package dnss.tools.pak;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Tool {
    final static Logger logger = Logger.getLogger(Tool.class);
    final static String jsonFile = "pak.json";
    private static JSONObject json;
    private static HashMap<String, Pattern> globalAllowMap = new HashMap<String, Pattern>();
    private static HashMap<String, Pattern> globalIgnoreMap = new HashMap<String, Pattern>();
    private static ArrayList<PakProperties> propertiesList = new ArrayList<PakProperties>();

    static {
        InputStream inputStream = Tool.class.getClassLoader().getResourceAsStream(jsonFile);
        Scanner scanner = new Scanner(inputStream);
        String jsonContents = scanner.useDelimiter("\\Z").next();
        json = new JSONObject(jsonContents);
    }

    private static HashMap<String, Pattern> loadPatternMap(JSONArray json) {
        HashMap<String, Pattern> patternMap = new HashMap<String, Pattern>();
        for (int i = 0; i < json.length(); i++) {
            String pattern = json.getString(i);
            patternMap.put(pattern, Pattern.compile(pattern));
            logger.info("Loaded pattern " + pattern);
        }

        return patternMap;
    }

    private static void loadGlobalAllowMap() {
        if (json.has("allow")) {
            logger.info("Loading global allow patterns...");
            globalAllowMap = loadPatternMap(json.getJSONArray("allow"));
            logger.info("Successfully loaded " + globalAllowMap.size() + " global allowed patterns.");
        } else {
            logger.info("No global allowed patterns have been loaded.");
        }
    }

    private static void loadGlobalIgnoreMap() {
        if (json.has("ignore")) {
            logger.info("Loading global ignore patterns...");
            globalIgnoreMap = loadPatternMap(json.getJSONArray("ignore"));
            logger.info("Successfully loaded " + globalIgnoreMap.size() + " global ignore patterns.");
        } else {
            logger.info("No global ignore patterns have been loaded.");
        }
    }

    private static void loadAllPakProperties() {
        JSONArray propertiesListJSON = json.getJSONArray("extract");
        for (int i = 0; i < propertiesListJSON.length(); i++) {
            JSONObject propertiesJSON = propertiesListJSON.getJSONObject(i);
            PakProperties properties = new PakProperties();
            properties.setFile(new File(propertiesJSON.getString("file")));
            if (! properties.getFile().exists()) {
                logger.warn("Could not find pak file " + properties.getFile().getPath());
                logger.warn("Skipping...");
                continue;
            }

            properties.setOutput(new File(propertiesJSON.getString("output")));

            if (propertiesJSON.has("extractDeleted")) {
                properties.setExtractDeleted(propertiesJSON.getBoolean("extractDeleted"));
            } else {
                properties.setExtractDeleted(false);
            }

            if (properties.canExtractDeleted()) {
                logger.info(properties.getFile().getPath() + " will extract files that are set as deleted.");
            } else {
                logger.info(properties.getFile().getPath() + " will NOT extract files that are set as deleted.");
            }

            logger.info("Found file " + properties.getFile().getPath());
            logger.info("Output directory is set to " + properties.getOutput().getPath());

            if (propertiesJSON.has("allow")) {
                HashMap<String, Pattern> map = new HashMap<String, Pattern>(globalAllowMap);
                logger.info("Loading " + properties.getFile().getPath() + " allow patterns");
                map.putAll(loadPatternMap(propertiesJSON.getJSONArray("allow")));
                properties.setAllow(map);
                logger.info(properties.getFile().getPath() + " has a total of " + map.size() + " allow patterns" +
                        " (including the global allow patterns)");
            } else {
                properties.setAllow(new HashMap<String, Pattern>(globalAllowMap));
                logger.info("Using global allow patterns for " + properties.getFile().getPath());
            }

            if (propertiesJSON.has("ignore")) {
                HashMap<String, Pattern> map = new HashMap<String, Pattern>(globalIgnoreMap);
                logger.info("Loading " + properties.getFile().getPath() + " ignore patterns");
                map.putAll(loadPatternMap(propertiesJSON.getJSONArray("ignore")));
                properties.setIgnore(map);
                logger.info(properties.getFile().getPath() + " has a total of " + map.size() + " ignore patterns" +
                        " (including the global ignore patterns)");
            } else {
                properties.setIgnore(new HashMap<String, Pattern>(globalIgnoreMap));
                logger.info("Using global ignore patterns for " + properties.getFile().getPath());
            }

            propertiesList.add(properties);
        }
    }

    public static void main(String[] args)  {
        loadGlobalAllowMap();
        loadGlobalIgnoreMap();
        loadAllPakProperties();

        for (PakProperties properties : propertiesList) {
            File file = properties.getFile();
            File output = properties.getOutput();
            logger.info("================================================");
            logger.info("Beginning to extract " + file.getPath());
            logger.info("================================================");
            try {
                if (! output.exists() && ! output.mkdirs()) {
                    logger.error("Could not create output directory " + output.getPath());
                    continue;
                }

                PakParser pakParser = new PakParser(properties);
                ArrayList<PakFile> pakFiles = pakParser.parse();

                if (pakFiles == null) {
                    continue;
                }

                for (PakFile pakFile : pakFiles) {
                    pakFile.extract();
                }
            } catch (IOException e) {
                logger.error(e);
            } catch (DataFormatException e) {
                logger.error(e);
            }
        }

    }
}
