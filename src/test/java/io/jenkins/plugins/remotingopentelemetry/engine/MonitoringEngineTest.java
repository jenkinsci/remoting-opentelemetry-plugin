package io.jenkins.plugins.remotingopentelemetry.engine;

import org.junit.After;
import org.junit.Test;

import java.util.Objects;
import java.util.stream.Stream;

public class MonitoringEngineTest {
    @Test
    public void testIsRunning() throws Exception {
        MonitoringEngine.launch();
        assert MonitoringEngine.isRunning();
        MonitoringEngine.terminate();
        assert !MonitoringEngine.isRunning();
        MonitoringEngine.launch();
        assert MonitoringEngine.isRunning();
    }

    @Test
    public void testOnlyOneMonitoringThread() throws Exception {
        MonitoringEngine.launch();
        MonitoringEngine.launch();
        ThreadGroup currentThreadGroup = Thread.currentThread().getThreadGroup();
        Thread[] siblingThreads = new Thread[currentThreadGroup.activeCount()];
        currentThreadGroup.enumerate(siblingThreads);
        Thread[] monitoringThreads = Stream.of(siblingThreads)
                .filter(t -> Objects.equals(t.getClass().getCanonicalName(), MonitoringEngine.class.getCanonicalName()))
                .toArray(Thread[]::new);
        assert monitoringThreads.length == 1;
    }

    @After
    public void tearDown() throws Exception {
        MonitoringEngine.terminate();
    }
}
