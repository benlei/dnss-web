package dnss.tools.pak;

import java.util.LinkedList;
import java.util.Queue;

public class PakFileQueue {
    private PakProperties properties;
    private Queue<Thread> queue;
    private int total = 0;

    public PakFileQueue(PakProperties properties) {
        this.properties = properties;
        queue = new LinkedList<Thread>();
    }

    public void enqueue(PakFile element) {
        ++total;
        Thread t = new Thread(element);
        t.setName("dnss.tools.pak");

        // every pak should have up to maxThreads attempting to extract
        if (total < properties.getMaxThreads()) {
            t.start();
        } else {
            queue.add(t);
        }
    }

    public synchronized void dequeue() {
        // start another thread, usually when another thread is about done
        if (! queue.isEmpty()) {
            queue.poll().start();
        }
    }

    public int size() {
        return queue.size();
    }

    public int total() {
        return total;
    }
}
