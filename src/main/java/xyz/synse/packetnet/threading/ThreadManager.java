package xyz.synse.packetnet.threading;

import java.util.concurrent.*;

public class ThreadManager {
    private static final ExecutorService service = Executors.newCachedThreadPool();

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
