package dnss.tools.commons;

import java.util.HashMap;

public class DNSS {
    private static HashMap<String, Object> properties = new HashMap<String, Object>();

    private DNSS() {
        // should not be allowed
    }

    public static synchronized void set(String name, Object obj) {
        String[] splits = name.split("\\.");
        if (splits.length == 1) {
            properties.put(name, obj);
        } else {
            int i;
            Properties prop = get(splits[0], Properties.class);
            for (i = 1; i < splits.length - 1; i++) {
                prop = prop.get(splits[i], Properties.class);
            }

            prop.set(splits[i], obj);
        }
    }

    public static synchronized void set(int idx, Object obj) {
        set(String.valueOf(idx), obj);
    }

    public static <T> T get(String name, Class<T> type) {
        return get(name, null, type);
    }

    public static <T> T get(int idx, Class<T> type) {
        return get(idx, null, type);
    }

    public static <T> T get(String name, T def, Class<T> type) {
        String[] splits = name.split("\\.");
        if (! properties.containsKey(splits[0])) {
            return def;
        }

        if (splits.length == 1) {
            return (T) properties.get(name);
        }

        int i;
        Properties prop =  (Properties) properties.get(splits[0]);
        for (i = 1; i < splits.length - 1; i++) {
            prop = prop.get(splits[i], Properties.class);
        }

        return prop.get(splits[i], type);
    }

    public static <T> T get(int idx, T def, Class<T> type) {
        return get(String.valueOf(idx), def, type);

    }

    public static boolean has(String name) {
        String[] splits = name.split("\\.");
        if (splits.length == 1) {
            return properties.containsKey(name);
        } else {
            int i;
            Properties prop =  (Properties) properties.get(splits[0]);
            for (i = 1; i < splits.length - 1; i++) {
                prop = prop.get(splits[i], Properties.class);
            }

            return prop.has(splits[i]);
        }
    }

    public static boolean has(int idx) {
        return has(String.valueOf(idx));
    }

    public static synchronized Properties add(String name) {
        if (has(name)) {
            throw new PropertiesExistException("Properties " + name + " already exists.");
        }

        Properties sub = new Properties(name, null);
        set(name, sub);
        return sub;
    }

    public static synchronized Properties add(int idx) {
        return add(String.valueOf(idx));
    }

    public static int size() {
        return properties.size();
    }
}
