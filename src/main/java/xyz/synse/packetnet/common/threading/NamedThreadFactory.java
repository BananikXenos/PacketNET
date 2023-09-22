package xyz.synse.packetnet.common.threading;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final int threadPriority; // Added configurable thread priority

    public NamedThreadFactory() {
        this(Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(int threadPriority) {
        group = Thread.currentThread().getThreadGroup();
        namePrefix = "ThreadPoolManager [P:" + poolNumber.getAndIncrement() + ",T:";
        this.threadPriority = threadPriority;
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement() + "]", 0);
        if (t.isDaemon())
            t.setDaemon(false);
        t.setPriority(threadPriority); // Set the configured thread priority
        return t;
    }
}
