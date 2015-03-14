package dnss.tools.dnt;

import dnss.tools.commons.Properties;
import dnss.tools.commons.ReadStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DNTParser {
    private Properties properties;

    public DNTParser(Properties properties) {
        this.properties = properties;
    }

    public void parse() throws IOException {
        ReadStream readStream = new ReadStream(properties.get("file", String.class));
        DNTAccumulator accumulator = properties.get("accumulator", DNTAccumulator.class);

        // # of cols EXCLUDING the Id column.
        int numCols = readStream.seek(4).readShort();
        int numRows = readStream.readInt();

        DNTTable table = new DNTTable();
        for (int i = 0; i < numCols; i++) {
            String fieldName = readStream.readString(readStream.readShort());
            table.addField(fieldName, DNT.resolve(readStream.read()));
        }

        Set<Map.Entry<String,DNT>> fields = table.getFields().entrySet();
        accumulator.accumulate(fields);
        for (int i = 0; i < numRows; i++) {
            HashMap<String, Object> row = new HashMap<String, Object>();
            for (Map.Entry<String, DNT> entry : fields) {
                row.put(entry.getKey(), entry.getValue().read(readStream));
            }
            accumulator.accumulate(row);
        }

        readStream.close();
    }
}
