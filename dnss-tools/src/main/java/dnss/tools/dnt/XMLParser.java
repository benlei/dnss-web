package dnss.tools.dnt;

import dnss.tools.commons.Pair;
import dnss.tools.commons.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.CharacterData;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class XMLParser implements Parser, Runnable {
    private final static Logger log = LoggerFactory.getLogger(DNTParser.class);

    private DNT dnt;

    public XMLParser(DNT dnt) {
        this.dnt = dnt;
    }

    @Override
    public void parse() throws IOException {
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(dnt.getLocation());
            document.getDocumentElement().normalize();
        } catch (Exception e) {
            log.error("Could not open XML document", e);
            return;
        }

        ArrayList<Pair<String, Types>> fieldList = new ArrayList<Pair<String, Types>>();
        DNTFields fields = new DNTFields(dnt);
        fieldList.add(new Pair(DNTFields.id, Types.INT));
        fieldList.add(new Pair<String, Types>("_Data", Types.STRING));
        fields.accumulate(fieldList.get(1));

        DNTEntries entries = new DNTEntries(dnt, fieldList);
        NodeList nodeList = document.getElementsByTagName("message");
        for (int i = 0; i < nodeList.getLength(); i++) {
            ArrayList<Object> values = new ArrayList<Object>();
            Element element = (Element) nodeList.item(i);
            CharacterData characterData = (CharacterData)element.getFirstChild();
            values.add(Integer.valueOf(element.getAttribute("mid")));
            values.add(characterData.getData());
            entries.accumulate(values);
        }


        File destination = dnt.getDestination();
        File destinationDir = destination.getParentFile();

        synchronized (DNTParser.LOCK) {
            if (!destinationDir.exists() && !destinationDir.mkdirs()) {
                throw new IOException("Unable to create directory " + destinationDir.getPath());
            }
        }

        FileWriter writer = new FileWriter(destination);
        writer.write(fields.dissipate().append(entries.dissipate()).toString());
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
