package dnss.tools.pak;

import dnss.tools.commons.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.zip.DataFormatException;


public class PakConsumer implements Consumer<PakFile>, Runnable {
    private static final Logger log = LoggerFactory.getLogger(PakConsumer.class);
    private PakItems items;
    private ExecutorService parserService;

    public PakConsumer(PakItems items, ExecutorService parserService) {
        this.items = items;
        this.parserService = parserService;
    }

    @Override
    public PakFile consume() {
        return items.poll();
    }

    @Override
    public void run() {
        PakFile pakFile;
        while ((pakFile=consume()) != null || ! parserService.isTerminated()) {
            if (pakFile == null) {
                Thread.yield(); // give up cpu
                continue;
            }

            try {
                Thread.currentThread().setName(pakFile.getPak().getId());
                pakFile.extract();
            } catch(IOException e) {
//                logger.error("Could not extract " + pakFile.getFilePath() + " from " + pakFile.getOutput().getPath(), e);
            } catch (DataFormatException e) {
//                logger.error("Could not extract zipped content " + pakFile.getFilePath() + " from " + pakFile.getOutput().getPath(), e);
            } finally {
                Thread.currentThread().setName("consumer");
            }

        }
    }
}
