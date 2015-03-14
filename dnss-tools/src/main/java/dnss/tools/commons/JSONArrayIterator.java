package dnss.tools.commons;

import java.util.Iterator;

/**
 * Created by Ben on 3/13/2015.
 */
public class JSONArrayIterator implements Iterator<Integer> {
    private int start = 0;
    private int max;

    public JSONArrayIterator(int max) {
        this.max = max;
    }

    @Override
    public boolean hasNext() {
        return start < max;
    }

    @Override
    public Integer next() {
        return start++;
    }
}
