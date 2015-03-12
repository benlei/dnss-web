package dnss.tools.dnt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DNTEntry {
    private HashMap<String, DNT> fields;
    private HashMap<String, Object> elements = new HashMap<String, Object>();

    public DNTEntry(HashMap<String, DNT> fields) {
        this.fields = fields;
    }

    private DNT getDNT(String key) {
        return fields.get(key);
    }

    public Object get(String key) {
        return elements.get(key);
    }

    public boolean put(String key, Object value) {
        if (! fields.containsKey(key)) {
            return false;
        }

        elements.put(key, value);
        return true;
    }
}
