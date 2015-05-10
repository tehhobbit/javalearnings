package net.tehhobbit;

import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class PollerPool {
    private final int nThreads;
    private final PoolWorker[] threads;
    private final LinkedList queue;
    private Logger log = Logger.getLogger(PollerPool.class.getName());

    public PollerPool(int nThreads) {
        this.nThreads = nThreads;
        queue = new LinkedList();
        threads = new PoolWorker[nThreads];
        log.info("Staring worker pool with " + nThreads + " threads");
        for (int i = 0; i < nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public void close() {
        log.info("Waiting for threads to finnish ");

        synchronized (queue) {
            for (int i = 0; i < nThreads; i++) {
                queue.addLast(null);
                queue.notify();
            }
        }
        for (PoolWorker p : threads) {

            try {
                p.join();
            } catch (InterruptedException e) {
                log.error("Interrupted");
            }
        }
        log.info("All threads done");
    }

    public void put(Runnable r) {
        synchronized (queue) {
            queue.addLast(r);
            queue.notifyAll();
        }
    }

    private class PoolWorker extends Thread {

        public void run() {
            Runnable poller;
            log.info("Starting worker " + this);
            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    try {

                        poller = (Runnable) queue.removeFirst();
                        log.debug(poller);
                        if (poller == null) {
                            break;
                        } else {
                            poller.run();
                        }
                    } catch (NoSuchElementException e) {
                        log.error("Something odd happened " + e.toString());
                    }
                }

            }
            log.info("Exiting thread " + this);
        }
    }
}
