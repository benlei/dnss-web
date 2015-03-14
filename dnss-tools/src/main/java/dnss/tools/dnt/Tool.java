package dnss.tools.dnt;

import dnss.tools.commons.JSONPropertiesParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Tool {
    private static String dntFile = "dnt.json";

    public static void main(String[] args) throws FileNotFoundException {
        InputStream inputStream;
        if (args.length == 1) {
            inputStream = new FileInputStream(args[0]);
        } else {
            inputStream = Tool.class.getClassLoader().getResourceAsStream(dntFile);
        }
        JSONPropertiesParser.parse(inputStream);


    }
}
