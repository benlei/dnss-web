package dnss.tools.commons;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Loads JSON properties into Properties
 */
public class JSONPropertiesParser {
    private final static Logger logger = Logger.getLogger(JSONPropertiesParser.class);

    public static void parse(InputStream stream) {
        logger.info("Initializing json parser...");
        Scanner scanner = new Scanner(stream);
        String jsonContents = scanner.useDelimiter("\\Z").next();
        JSONObject json = new JSONObject(jsonContents);
        parse(json, JSONObject.class, json.keys(), String.class, null);
    }

    private static <T, I> void parse(T json, Class<T> objType, Iterator<I> iterator, Class<I> iterType, Properties properties) {
        while (iterator.hasNext()) {
            I idx = iterator.next();
            String name = String.valueOf(idx);
            try {
                JSONObject obj = (JSONObject)objType.getDeclaredMethod("getJSONObject", iterType).invoke((T)json, idx);
                parse(obj, JSONObject.class, obj.keys(), String.class, addProperties(name, properties));
                continue;
            } catch (JSONException e) { // means its not a JSONArray
            } catch (Exception e) {
            }

            try {
                JSONArray arr = (JSONArray)objType.getDeclaredMethod("getJSONArray", iterType).invoke((T)json, idx);
                parse(arr, JSONArray.class, new JSONArrayIterator(arr.length()), Integer.TYPE, addProperties(name, properties));
                continue;
            } catch (JSONException e) { // means its not a JSONArray
            } catch (Exception e) {
            }

            try {
                boolean val = (Boolean)objType.getDeclaredMethod("getBoolean", iterType).invoke((T)json, idx);
                setProperties(name, val, properties);
                logger.info(resolve(name, properties) + " = " + val);
                continue;
            } catch (JSONException e) { // means its not a bool
            } catch (Exception e) {
            }

            try {
                int val = (Integer)objType.getDeclaredMethod("getInt", iterType).invoke((T)json, idx);
                setProperties(name, val, properties);
                logger.info(resolve(name, properties) + " = " + val);
                continue;
            } catch (JSONException e) {  // means its not an int
            } catch (Exception e) {
            }


            try {
                String val = (String) objType.getDeclaredMethod("getString", iterType).invoke((T)json, idx);
                setProperties(name, val, properties);
                logger.info(resolve(name, properties) + " = " + val);
            } catch (Exception e) {
            }
        }
    }

    private static Properties addProperties(String name, Properties properties) {
        if (properties == null) {
            return DNSS.add(name);
        } else {
            return properties.add(name);
        }
    }


    private static Properties addProperties(int idx, Properties properties) {
        return addProperties(String.valueOf(idx), properties);
    }

    private static void setProperties(String name, Object value, Properties properties) {
        if (properties == null) {
            DNSS.set(name, value);
        } else {
            properties.set(name, value);
        }
    }

    private static void setProperties(int idx, Object value, Properties properties) {
        setProperties(String.valueOf(idx), value, properties);
    }

    private static String resolve(String name, Properties properties) {
        if (properties == null) {
            return name;
        } else {
            return properties.resolve(name);
        }
    }

    private static String resolve(int idx, Properties properties) {
        return resolve(String.valueOf(idx), properties);
    }
}
