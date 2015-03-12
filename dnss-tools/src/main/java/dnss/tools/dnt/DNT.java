package dnss.tools.dnt;

import dnss.tools.commons.ReadStream;

import java.io.IOException;

public enum DNT {
    STRING (String.class),
    BOOL   (Boolean.class),
    INT    (Integer.class),
    FLOAT  (Float.class),
    DOUBLE (Float.class); //a double that is single precision isn't a double...

    public final Class CLASS;

    DNT(Class CLASS) {
        this.CLASS = CLASS;
    }

    public static DNT resolve(int b) {
        switch (b) {
            case 1: return STRING;
            case 2: return BOOL;
            case 3: return INT;
            case 4: return FLOAT;
            case 5: return DOUBLE;
            default: throw new RuntimeException("Cannot resolve type " + b);
        }
    }

    public Object read(ReadStream readStream) throws IOException {
        switch (this) {
            case STRING: return readStream.readString(readStream.readShort());
            case BOOL: return readStream.readInt() != 0;
            case INT: return readStream.readInt();
            case FLOAT: case DOUBLE: return readStream.readFloat();
            default: throw new RuntimeException("Unknown DNT enum type!");
        }

    }
}