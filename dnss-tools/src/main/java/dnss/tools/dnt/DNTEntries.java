package dnss.tools.dnt;

import dnss.tools.commons.Accumulator;
import dnss.tools.commons.Pair;

import java.util.ArrayList;

public class DNTEntries implements Accumulator<ArrayList<Object>, StringBuilder> {
    private StringBuilder buf = new StringBuilder();
    private ArrayList<Pair<String, Types>> fields;

    public DNTEntries(DNT dnt, ArrayList<Pair<String, Types>> fields) {
        this.fields = fields;

        buf.append("INSERT INTO " + dnt.getId() + " (");
        for (Pair<String, Types> pair : fields) {
            buf.append(pair.getLeft());
            buf.append(',');
        }

        buf.deleteCharAt(buf.length() - 1);
        buf.append(") VALUES \n");
    }

    @Override
    public void accumulate(ArrayList<Object> element) {
        buf.append("  (" + element.get(0));
        for (int i = 1; i < element.size(); i++) {
            buf.append(',');
            switch (fields.get(i).getRight()) {
                case STRING:
                    String insert = ((String)element.get(i)).replaceAll("'", "''");
                    insert = insert.isEmpty() ? "null" : "'" + insert + "'";
                    buf.append(insert);
                    break;
                default:
                    buf.append(fields.get(i).getRight().TYPE.cast(element.get(i)));
                    break;
            }
        }
        buf.append("),\n");
    }

    @Override
    public StringBuilder dissipate() {
        StringBuilder builder = buf;
        builder.delete(builder.length() - 2, builder.length());
        builder.append(';');
        buf = null; // if you dissipate, can't use this object anymore
        return builder;
    }
}
