package dnss.tools.dnt;

import dnss.tools.commons.Pair;
import dnss.tools.commons.ReadStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DNTParser implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(DNTParser.class);
    private final static Object LOCK = new Object();
    private DNT dnt;

    public DNTParser(DNT dnt) {
        this.dnt = dnt;
    }

    public void parse() throws IOException {
        ReadStream readStream = new ReadStream(dnt.getLocation());
        DNTFields fields = new DNTFields(dnt);

        // # of cols EXCLUDING the Id column.
        int numCols = readStream.seek(4).readShort();
        int numRows = readStream.readInt();

        // could also use a linkedhashmap
        ArrayList<Pair<String, Types>> fieldList = new ArrayList<Pair<String, Types>>();
        fieldList.add(new Pair<String, Types>("id", Types.INT));
        for (int i = 0; i < numCols; i++) {
            String fieldName = readStream.readString(readStream.readShort());
            fieldName = fieldName.substring(1);

            Types type = Types.resolve(readStream.read());
            Pair<String, Types> pair = new Pair<String, Types>(fieldName, type);
            fields.accumulate(pair);
            fieldList.add(pair);
        }

        DNTEntries entries = new DNTEntries(dnt, fieldList);

        for (int i = 0; i < numRows; i++) {
            ArrayList<Object> values = new ArrayList<Object>();
            for (Pair<String, Types> field: fieldList) {
                values.add(field.getRight().read(readStream));
            }

            entries.accumulate(values);
        }

        readStream.close();

        File destination = dnt.getDestination();
        File destinationDir = destination.getParentFile();

        synchronized (LOCK) {
            if (!destinationDir.exists() && !destinationDir.mkdirs()) {
                throw new IOException("Unable to create directory " + destinationDir.getPath());
            }
        }

        FileWriter writer = new FileWriter(destination);
        writer.write(fields.dissipate().toString());
        writer.write(entries.dissipate().toString());
        writer.close();
    }

    @Override
    public void run() {
        try {
            Thread.currentThread().setName(dnt.getId());
            parse();
            log.info(dnt.getLocation().getPath() + " has successfully converted to " + dnt.getDestination().getPath());
        } catch (IOException e) {
            log.error("There was an error when parsing " + dnt.getLocation().getPath(), e);
        }
    }
}
