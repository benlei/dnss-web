package dnss.tools.pak;

import dnss.tools.commons.Items;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PakItems implements Items<PakFile, PakFile> {
    private ConcurrentLinkedQueue<PakFile> queue = new ConcurrentLinkedQueue<PakFile>();

    @Override
    public void add(PakFile item) {
        queue.add(item);
    }

    @Override
    public PakFile poll() {
        return queue.poll();
    }
}
