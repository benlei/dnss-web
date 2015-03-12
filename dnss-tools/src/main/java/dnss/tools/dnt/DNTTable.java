package dnss.tools.dnt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class DNTTable {
    private Map<String, DNT> fields = new LinkedHashMap<String, DNT>();
    private ArrayList<Map<String, Object>> entries = new ArrayList<Map<String, Object>>();
    private Map<String, DNT> unmodifiableFields;
    private boolean clearMapCache = true;

    public DNTTable() {
        addField("_Id", DNT.INT);
    }

    // note: this also destroys any existing data from the arraylist
    public void addField(String name, DNT type) {
        fields.put(name.replaceAll("([A-Z])", "_$1").toLowerCase().substring(2), type);
        clearMapCache = true;
    }

    public Map<String, DNT> getFields() {
        if (clearMapCache) {
            if (unmodifiableFields != null) {
                // should not happen at all, but in place just in case

            }

            clearMapCache = false;
            unmodifiableFields = Collections.unmodifiableMap(fields);
        }

        return unmodifiableFields;
    }

    public void add(Map<String, Object> entry) {
        entries.add(entry);
    }
}
