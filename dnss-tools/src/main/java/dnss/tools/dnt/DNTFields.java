package dnss.tools.dnt;

import dnss.tools.commons.Accumulator;

import dnss.tools.commons.Pair;

/**
 * Created by Ben on 3/16/2015.
 */
public class DNTFields implements Accumulator<Pair<String, Types>, StringBuilder> {
    private StringBuilder buf = new StringBuilder();
    public static String id = "_ID";

    public DNTFields(DNT dnt) {
        buf.append("DROP TABLE IF EXISTS " + dnt.getId() + ";\n");
        buf.append("CREATE TABLE " + dnt.getId() + "(\n");
        buf.append("  " + id + " serial PRIMARY KEY");

    }

    @Override
    public void accumulate(Pair<String, Types> item) {
        buf.append(",\n");
        buf.append("  " + item.getLeft() + " " + item.getRight().FIELD);
    }

    @Override
    public StringBuilder dissipate() {
        StringBuilder newBuf = new StringBuilder(buf);
        newBuf.append(");\n");
        return newBuf;
    }
}
