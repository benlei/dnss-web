package dnss.tools.dnt;

import dnss.tools.commons.Accumulator;
import dnss.tools.commons.Pair;

import java.util.ArrayList;

public class DNTEntries implements Accumulator<ArrayList<Object>, StringBuilder> {
    private StringBuilder buf = new StringBuilder();
    private DNT dnt;
    private ArrayList<Pair<String, Types>> fields;
    private String columns;

    public DNTEntries(DNT dnt, ArrayList<Pair<String, Types>> fields) {
        this.dnt = dnt;
        this.fields = fields;

        StringBuilder columnsBuf = new StringBuilder();
        columnsBuf.append('(');
        for (Pair<String, Types> pair : fields) {
            columnsBuf.append(pair.getLeft());
            columnsBuf.append(',');
        }

        columnsBuf.deleteCharAt(columnsBuf.length() - 1);
        columnsBuf.append(')');
        columns = columnsBuf.toString();
    }

    @Override
    public void accumulate(ArrayList<Object> element) {
        buf.append("INSERT INTO " + dnt.getId() + " " + columns + " VALUES (");
        buf.append(element.get(0)); // the id
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
        buf.append(");\n");
    }

    @Override
    public StringBuilder dissipate() {
        StringBuilder newBuf = new StringBuilder(buf);
        newBuf.deleteCharAt(newBuf.length() - 1);
        return newBuf;
    }
}
