package net.tehhobbit;

import org.apache.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class PollerPool {
    private final int nThreads;
    private final PoolWorker[] threads;
    private BlockingQueue queue = new ArrayBlockingQueue(1024);
    private static Logger log = Logger.getLogger(PollerPool.class.getName());

    public PollerPool(int nThreads) {
        this.nThreads = nThreads;
        threads = new PoolWorker[nThreads];
        log.info("Staring worker pool with " + nThreads + " threads");
        for (int i = 0; i < nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public void close() {
        log.info("Waiting for threads to finnish ");

        for (int i = 0; i < nThreads; i++) {
            threads[i].setRunning(false);
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
        try {
            queue.put(r);
        } catch (InterruptedException e){

        }

    }

    private class PoolWorker extends Thread {
        public Boolean running = false;

        public void setRunning(Boolean running) {
            this.running = running;
        }
        public Boolean getRunning() {
            return running;
        }
        public void run() {
            Runnable poller;
            log.info("Starting worker " + this);
            running = true;
            while (running) {
                try {

                    Object next =  queue.poll(400, TimeUnit.MICROSECONDS);
                    if(next == null) {
                        continue;
                    } else {
                        poller = (Runnable) next;
                        poller.run();
                    }
                } catch (InterruptedException e) {
                    break;
                }
                log.debug(poller);
                try {
                    TimeUnit.SECONDS.sleep((long) (Math.random() * 10));
                } catch (InterruptedException e){

                }


            }
            log.info("Exiting thread " + this);
        }

    }
}
