package dnss.tools.pak;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class PakExtractor {
    private static final Logger log = LoggerFactory.getLogger(PakExtractor.class);

    private final static String props = "pak.properties";

    private static ArrayList<Pattern> loadPatternList(Properties properties, String starting) {
        ArrayList<Pattern> patterns = new ArrayList<Pattern>();
        for (String name : properties.stringPropertyNames()) {
            if (name.startsWith(starting)) {
                Pattern pattern = Pattern.compile(properties.getProperty(name));
                patterns.add(pattern);
            }
        }

        return patterns;
    }

    private static int totalAliveFutures(ArrayList<Future> futures) {
        int total = 0;
        for (Future future : futures) {
            if (! future.isDone()) {
                total++;
            }
        }

        return total;
    }

    public static void main(String[] args) throws Exception {
        InputStream is;
        if (0 < args.length) {
            is = new FileInputStream(args[0]);
        } else {
            is = PakExtractor.class.getClassLoader().getResourceAsStream(props);
        }

        Properties properties = new Properties();
        byte[] propertiesBytes = new byte[is.available()];
        is.read(propertiesBytes);
        properties.load(new StringReader((new String(propertiesBytes)).replace("\\","\\\\")));
        is.close();

        // set the system properties
        int maxThreads = Integer.valueOf(properties.getProperty("system.max.threads", System.getProperty("max.threads", "1")));
        System.setProperty("max.threads", String.valueOf(maxThreads));
        System.setProperty("log.deleted", properties.getProperty("system.log.deleted", System.getProperty("log.deleted", "true")));
        System.setProperty("log.ignored", properties.getProperty("system.log.ignored", System.getProperty("log.ignored", "true")));
        System.setProperty("log.extracted", properties.getProperty("system.log.extracted", System.getProperty("log.extracted", "true")));

        String destination = properties.getProperty("global.destination", null);
        boolean flatten = Boolean.valueOf(properties.getProperty("global.flatten", "false"));
        boolean extractDeleted = Boolean.valueOf(properties.getProperty("global.extract.deleted", "false"));

        ArrayList<Pattern> allowPatterns = loadPatternList(properties, "global.allow");
        ArrayList<Pattern> ignorePatterns = loadPatternList(properties, "global.ignore");

        Pak base = new Pak();
        base.setDestination(destination == null ? null : new File(destination));
        base.setFlatten(flatten);
        base.setExtractDeleted(extractDeleted);
        base.setAllow(allowPatterns);
        base.setIgnore(ignorePatterns);

        HashMap<String, Pak> pakMap = new HashMap<String, Pak>();
        for (String name : properties.stringPropertyNames()) {
            if (!name.startsWith("pak.")) {
                continue;
            }

            String pakId = name.substring(4);
            pakId = pakId.substring(0, pakId.indexOf('.'));
            if (! pakMap.containsKey(pakId)) {
                Pak pak = (Pak) base.clone();
                String prefix = "pak." + pakId + ".";

                pak.setId(pakId);

                allowPatterns = loadPatternList(properties, prefix + "allow");
                if (! allowPatterns.isEmpty()) {
                    pak.setAllow(allowPatterns);
                }

                ignorePatterns = loadPatternList(properties, prefix + "ignore");
                if (! ignorePatterns.isEmpty()) {
                    pak.setIgnore(ignorePatterns);
                }

                pak.setLocation(new File(properties.getProperty(prefix + "location")));

                if (properties.containsKey(prefix + "destination")) {
                    pak.setDestination(new File(properties.getProperty(prefix + "destination")));
                }

                if (properties.containsKey(prefix + "flatten")) {
                    pak.setFlatten(Boolean.valueOf(properties.getProperty(prefix + "flatten", "false")));
                }

                if (properties.containsKey(prefix + "extractDeleted")) {
                    pak.setExtractDeleted(Boolean.valueOf(properties.getProperty(prefix + "extractDeleted", "false")));
                }

                pakMap.put(pakId, pak);
            }
        }

        // Properties Output
        log.info("===================================================================");
        log.info("PakExtractor Properties");
        log.info("===================================================================");
        log.info("system.max.threads = " + maxThreads);
        log.info("system.log.deleted = " + System.getProperty("log.deleted"));
        log.info("system.log.ignored = " + System.getProperty("log.ignored"));
        log.info("system.log.extracted = " + System.getProperty("log.extracted"));
        for (Pak pak: pakMap.values()) {
            log.info("pak." + pak.getId() + ".file = " + pak.getLocation().getPath());
            log.info("pak." + pak.getId() + ".destination = " + pak.getDestination().getPath());
            log.info("pak." + pak.getId() + ".extractDeleted = " + pak.isExtractDeleted());
            log.info("pak." + pak.getId() + ".flatten = " + pak.isFlatten());
            for (Pattern pattern : pak.getAllow()) {
                log.info("pak." + pak.getId() + ".allow = " + pattern.pattern());
            }
            for (Pattern pattern : pak.getIgnore()) {
                log.info("pak." + pak.getId() + ".ignore = " + pattern.pattern());
            }
        }


        PakItems pakItems = new PakItems();
        ExecutorService parserService = Executors.newFixedThreadPool(maxThreads);
        ArrayList<Future> parserFutures = new ArrayList<Future>();
        long startTime = System.currentTimeMillis();
        for (Pak pak: pakMap.values()) {
            PakParser parser = new PakParser(pak, pakItems);
            parserFutures.add(parserService.submit(parser));
        }

        parserService.shutdown();

        ExecutorService consumerService = Executors.newFixedThreadPool(maxThreads);

        // make sure that no more than max threads are allowed (excluding this one)
        while (maxThreads < totalAliveFutures(parserFutures)) {
            Thread.yield();
        }

        ArrayList<Future> consumerFutures = new ArrayList<Future>();
        boolean yield;
        while (consumerFutures.size() < maxThreads) {
            int totalFree = maxThreads - totalAliveFutures(parserFutures) - consumerFutures.size();
            yield = true;
            if (0 < totalFree) {
                PakConsumer consumer = new PakConsumer(pakItems, parserService);
                consumerFutures.add(consumerService.submit(consumer));
                yield = false;
            }

            if (yield) {
                Thread.yield();
            }
        }

        consumerService.shutdown();

        try {
            consumerService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // something went wrong
        }

        long endTime = System.currentTimeMillis();

        log.info("===================================================================");
        log.info("Extraction Summary");
        log.info("===================================================================");
        for (Pak pak: pakMap.values()) {
            log.info(pak.getId() + ".totalFiles = " + pak.getTotalFiles());
            log.info(pak.getId() + ".totalExtractedFiles = " + pak.getTotalExtractedFiles());
            log.info(pak.getId() + ".totalSkippedFiles = " + pak.getTotalSkippedFiles());
            if (pak.isExtractDeleted()) {
                log.info(pak.getId() + ".totalDeletedFiles = " + pak.getTotalDeletedFiles());
            }
        }

        log.info("Total execution time: " + (endTime - startTime) + " ms");
    }
}
