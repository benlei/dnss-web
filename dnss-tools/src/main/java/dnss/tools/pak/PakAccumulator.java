package dnss.tools.pak;

import dnss.tools.commons.Accumulator;
import dnss.tools.commons.DNSS;
import java.util.LinkedList;

public class PakAccumulator implements Accumulator<PakFile> {
    private LinkedList<Thread> queue = new LinkedList<Thread>();
    private int total = 0;

    @Override
    public synchronized void accumulate(PakFile element) {
        ++total;
        Thread t = new Thread(element);
        t.setName("dnss.tools.pak");

        // every pak should have up to maxThreads attempting to extract
        if (total < DNSS.get("maxThreads", 1, Integer.TYPE)) {
            t.start();
        } else {
            queue.add(t);
        }
    }

    @Override
    public synchronized void dissipate() {
        // start another thread, usually when another thread is about done
        if (! queue.isEmpty()) {
            Thread t = queue.poll();
            t.start();
        }
    }

    public int total() {
        return total;
    }
}
