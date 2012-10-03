/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apiwatch.models.APIScope;

public class Analyser {

    static Logger LOGGER = Logger.getLogger(Analyser.class.getName());
    static Map<String, LanguageAnalyser> ANALYSERS_BY_FILEEXT = new HashMap<String, LanguageAnalyser>();
    static Map<String, LanguageAnalyser> ANALYSERS_BY_LANGUAGE = new HashMap<String, LanguageAnalyser>();

    static void discoverAnalysers() {
        ServiceLoader<LanguageAnalyser> loader = ServiceLoader.load(LanguageAnalyser.class);
        LOGGER.trace("Discovering LanguageAnalyser implementations in class path...");
        for (LanguageAnalyser impl : loader) {
            LOGGER.debug(String.format("Found %s. File extensions: %s", impl.getClass(), impl.fileExtensions()));
            for (String fileExt : impl.fileExtensions()) {
                ANALYSERS_BY_FILEEXT.put(fileExt, impl);
            }
            ANALYSERS_BY_LANGUAGE.put(impl.language(), impl);
        }
    }

    public static LanguageAnalyser getAnalyser(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName cannot be 'null'.");
        }
        String fileExt = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return ANALYSERS_BY_FILEEXT.get(fileExt);
    }

    public static Map<String, LanguageAnalyser> getAllAnalysers() {
        return ANALYSERS_BY_LANGUAGE;
    }

    public static APIScope analyse(Collection<String> files, Map<String, Object> options)
            throws InterruptedException
    {
        discoverAnalysers();
        int jobs;
        if (options != null && options.containsKey("jobs")) {
            jobs = (Integer) options.get("jobs");
        } else {
            jobs = Runtime.getRuntime().availableProcessors();
        }

        Deque<String> filesQueue = new LinkedBlockingDeque<String>(files);
        BlockingQueue<APIScope> scopesQueue = new LinkedBlockingQueue<APIScope>();
        
        LOGGER.trace(String.format("Spawning %s worker threads", jobs));
        List<Worker> workers = new ArrayList<Analyser.Worker>();
        for (int i = 0; i < jobs; i++) {
            Worker worker = new Worker(filesQueue, scopesQueue, options);
            worker.start();
            workers.add(worker);
        }
        for (Worker worker : workers) {
            worker.join();
        }
        
        LOGGER.trace("Merging parsing results...");
        APIScope globalScope = new APIScope();
        for (APIScope scope : scopesQueue) {
            globalScope.update(scope);
        }

        return globalScope;
    }

    private static class Worker extends Thread {

        Deque<String> filesQueue;
        BlockingQueue<APIScope> scopesQueue;
        Map<String, Object> options;

        public Worker(Deque<String> filesQueue, BlockingQueue<APIScope> scopesQueue,
                Map<String, Object> options)
        {
            super();
            this.filesQueue = filesQueue;
            this.scopesQueue = scopesQueue;
            this.options = options;
        }

        @Override
        public void run() {
            while (!filesQueue.isEmpty()) {
                try {
                    String file = filesQueue.remove();
                    LanguageAnalyser analyser = getAnalyser(file);
                    if (analyser != null) {
                        LOGGER.trace(String.format("Analysing '%s'...", file));
                        APIScope apiScope = analyser.analyse(file, options);
                        LOGGER.trace(String.format("Analysed '%s'", file));
                        scopesQueue.put(apiScope);
                    } else {
                        LOGGER.warn(String.format("Unknown extension '%s'", file));
                    }
                } catch (NoSuchElementException e) {
                    break;
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, "", e);
                }
            }
        }

    } // Worker

} // Analyser
