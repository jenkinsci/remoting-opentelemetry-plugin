package io.jenkins.plugins.remotingopentelemetry.engine;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Controls collecting and providing telemetry data.
 * One agent cannot run more than one MonitoringEngine thread.
 */
public final class MonitoringEngine extends Thread {
    /**
     * Launches the Monitoring engine.
     * The current engine will be killed if exists.
     * @throws InterruptedException
     */
    @Nullable
    static public synchronized void launch() throws InterruptedException {
        Thread current = MonitoringEngine.current();
        if (current != null) {
            current.interrupt();
            current.join();
        }
        current = new MonitoringEngine();
        current.start();
    }

    /**
     * Terminates the current Monitoring engine if exists.
     * @throws InterruptedException
     */
    static public void terminate() throws InterruptedException {
        Thread current = MonitoringEngine.current();
        if (current == null) return;
        current.interrupt();
        current.join();
    }

    /**
     * @return {@code true} if {@link MonitoringEngine} is running.
     */
    static public boolean isRunning() {
        return current() != null;
    }

    /**
     * Returns running {@link MonitoringEngine}.
     *
     * We cannot cast the return value to {@link MonitoringEngine} because
     * the classloader of running {@link MonitoringEngine} can be different
     * from that of {@link MonitoringEngine} we want to cast into.
     * @return null if there is no running {@link MonitoringEngine}
     */
    @Nullable
    static private Thread current() {
        ThreadGroup currentThreadGroup = Thread.currentThread().getThreadGroup();
        Thread[] siblingThreads = new Thread[currentThreadGroup.activeCount()];
        currentThreadGroup.enumerate(siblingThreads);
        Thread[] monitoringThreads =  Stream.of(siblingThreads)
                .filter(MonitoringEngine::isMonitoringEngine)
                .toArray(Thread[]::new);
        if (monitoringThreads.length < 1)  return null;
        return monitoringThreads[0];
    }

    /**
     * @param thread thread to check
     * @return {@code true} if given thread is {@link MonitoringEngine}
     */
    static private boolean isMonitoringEngine(Thread thread){
        // Checked by the canonical name in case the different classloaders are used.
        return Objects.equals(
                thread.getClass().getCanonicalName(),
                MonitoringEngine.class.getCanonicalName()
        );
    }

    /**
     * Disable the instantiation outside this class.
     */
    private MonitoringEngine () {
        setDaemon(true);
        setName("Monitoring Engine");
        // TODO: implements monitoring functionalities
    }

    @Override
    public void run() {
        try {
            block();
        } catch (InterruptedException e) {
            // TODO: finalize (remove listeners, etc...)
        }
    }

    /**
     * Blocks the current thread forever
     * @throws InterruptedException
     */
    private void block() throws InterruptedException {
        while(true) {
            Thread.sleep(Long.MAX_VALUE); // 292 million years
        }
    }
}
