package xyz.synse.packetnet.threading;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadManager {
    private static final ExecutorService service = Executors.newCachedThreadPool();

    public static Future<?> launchThread(final Runnable runnable) {
        return service.submit(runnable);
    }

    public static void waitForCompletion(final Future<?> future) throws ExecutionException, InterruptedException {
        future.get();
    }

    public static void shutdown() {
        service.shutdownNow();
    }
}
