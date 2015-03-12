package dnss.tools.dnt;

import dnss.tools.commons.ReadStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DNTParser {
    private DNTProperties properties;

    public DNTParser(DNTProperties properties) {
        this.properties = properties;
    }

    public void parse() throws IOException {
        ReadStream readStream = new ReadStream(properties.getFile());

        // # of cols EXCLUDING the Id column.
        int numCols = readStream.seek(4).readShort();
        int numRows = readStream.readInt();

        DNTTable table = new DNTTable();
        for (int i = 0; i < numCols; i++) {
            String fieldName = readStream.readString(readStream.readShort());
            table.addField(fieldName, DNT.resolve(readStream.read()));
        }

        for (int i = 0; i < numRows; i++) {
            HashMap<String, Object> row = new HashMap<String, Object>();
            for (Map.Entry<String, DNT> entry : table.getFields().entrySet()) {
                row.put(entry.getKey(), entry.getValue().read(readStream));
            }
            table.add(row);
        }

        readStream.close();
    }
}
