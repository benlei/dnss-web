package dnss.tools.dnt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class DNTTable {
    private HashMap<String, DNT> fields = new LinkedHashMap<String, DNT>();
    private ArrayList<DNTEntry> entries = new ArrayList<DNTEntry>();

    public DNTTable() {
        addField("_Id", DNT.INT);
    }

    // note: this also destroys any existing data from the arraylist
    public void addField(String name, DNT type) {
        fields.put(name.replaceAll("([A-Z])", "_$1").toLowerCase().substring(2), type);
    }

    public HashMap<String, DNT> getFields() {
        return fields;
    }

    public DNTEntry newEntry() {
        DNTEntry entry = new DNTEntry(fields);
        entries.add(entry);
        return entry;
    }
}
