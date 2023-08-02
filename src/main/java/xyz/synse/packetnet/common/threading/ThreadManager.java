package xyz.synse.packetnet.common.threading;

import java.util.concurrent.*;

public class ThreadManager {
    private static final ExecutorService service = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      0L, TimeUnit.MILLISECONDS,
                                      new SynchronousQueue<Runnable>());

    public static Future<?> launchThread(final Runnable runnable) {
        return service.submit(runnable);
    }

    public static void waitForCompletion(final Future<?> future) throws ExecutionException, InterruptedException {
        future.get();
    }

    public static void shutdownNow() {
        service.shutdownNow();
    }

    public static void shutdown() {
        service.shutdown();
    }

    public static boolean waitForShutdown(long time, TimeUnit unit) throws InterruptedException {
        return service.awaitTermination(time, unit);
    }
}
