package dnss.tools.dnt;

import java.io.IOException;
import java.nio.ByteBuffer;

public enum Types {
    STRING (String.class, "text"),
    BOOL   (Boolean.class, "boolean"),
    INT    (Integer.class, "integer"),
    FLOAT  (Float.class, "real"),
    DOUBLE (Float.class, "real"); //a double that is single precision isn't a double...

    public final Class TYPE;
    public final String FIELD;

    Types(Class TYPE, String FIELD) {
        this.TYPE = TYPE;
        this.FIELD = FIELD;
    }

    public static Types resolve(int b) {
        switch (b) {
            case 1: return STRING;
            case 2: return BOOL;
            case 3: return INT;
            case 4: return FLOAT;
            case 5: return DOUBLE;
            default: throw new RuntimeException("Cannot resolve type " + b);
        }
    }

    public Object getBufferToObject(ByteBuffer buf) throws IOException {
        switch (this) {
            case STRING:
                short len = buf.getShort();
                byte[] bytes = new byte[len];
                buf.get(bytes);
                return new String(bytes);
            case BOOL: return buf.getInt() != 0;
            case INT: return buf.getInt();
            case FLOAT: case DOUBLE: return buf.getFloat();
            default: throw new RuntimeException("Unknown DNT enum type!");
        }

    }
}