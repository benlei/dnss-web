package dnss.tools.commons;

import java.util.HashMap;

public class Properties {
    private HashMap<String, Object> properties = new HashMap<String, Object>();
    private String parentName;
    private Properties parent;

    protected Properties(String parentName, Properties parent) {
        this.parentName = parentName;
        this.parent = parent;
    }

    public synchronized void set(String name, Object obj) {
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

    public synchronized void set(int idx, Object obj) {
        set(String.valueOf(idx), obj);
    }

    public <T> T get(String name, Class<T> type) {
        return get(name, null, type);
    }

    public <T> T get(int idx, Class<T> type) {
        return get(idx, null, type);
    }

    public <T> T get(String name, T def, Class<T> type) {
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

    public <T> T get(int idx, T def, Class<T> type) {
        return get(String.valueOf(idx), def, type);

    }

    public boolean has(String name) {
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

    public boolean has(int idx) {
        return has(String.valueOf(idx));
    }

    public synchronized Properties add(String name) {
        if (has(name)) {
            throw new PropertiesExistException("Properties " + name + " already exists.");
        }

        Properties sub = new Properties(name, this);
        set(name, sub);
        return sub;
    }

    public synchronized Properties add(int idx) {
        return add(String.valueOf(idx));
    }

    public int size() {
        return properties.size();
    }

    public String resolve(String key) {
        Properties current = this;
        while (current.parent != null) {
            key = current.parentName + "." + key;
            current = current.parent;
        }

        key = current.parentName + "." + key;
        return key;
    }
}
