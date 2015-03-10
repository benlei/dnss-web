package dnss.tools.pak;

import dnss.tools.commons.Accumulator;

import java.util.LinkedList;
import java.util.Queue;

public class PakAccumulator implements Accumulator<PakFile> {
    private PakProperties properties;
    private Queue<Thread> queue;
    private int size = 0;

    public PakAccumulator(PakProperties properties) {
        this.properties = properties;
        queue = new LinkedList<Thread>();
    }

    @Override
    public void accumulate(PakFile element) {
        ++size;
        Thread t = new Thread(element);
        t.setName("dnss.tools.pak");

        // every pak should have up to maxThreads attempting to extract
        if (size < properties.getMaxThreads()) {
            t.start();
        } else {
            queue.add(t);
        }
    }

    public synchronized void dissipate() {
        // start another thread, usually when another thread is about done
        if (! queue.isEmpty()) {
            queue.poll().start();
        }
    }

    public int size() {
        return queue.size();
    }

    @Override
    public int accumulations() {
        return size;
    }
}
