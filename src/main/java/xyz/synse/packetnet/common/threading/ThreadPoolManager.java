package xyz.synse.packetnet.common.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.synse.packetnet.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadPoolManager {
    private final Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);
    private final List<Future<?>> futures = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new NamedThreadFactory()) {
        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);

            if(!(r instanceof Future<?> future)) return;

            futures.add(future);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            if(!(r instanceof Future<?> future)) return;

            futures.remove(future);

            if(t == null) return;

            logger.warn("Future exited with an exception", t);
        }
    };

    public Future<?> submit(Runnable runnable) {
        return executorService.submit(runnable);
    }

    public void shutdown(boolean interrupt) {
        executorService.shutdown();

        for(Future<?> future : futures){
            future.cancel(interrupt);
        }
        futures.clear();
    }

    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    public void awaitTermination() throws InterruptedException {
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
}
