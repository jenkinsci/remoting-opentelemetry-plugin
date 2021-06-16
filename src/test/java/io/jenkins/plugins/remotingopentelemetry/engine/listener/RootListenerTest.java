package io.jenkins.plugins.remotingopentelemetry.engine.listener;

import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.jenkins.plugins.remotingopentelemetry.engine.span.ChannelKeepAliveSpan;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RootListenerTest {
    RootListener rootListener;

    @Before
    public void setup() throws Exception {
        InMemorySpanExporter exporter = InMemorySpanExporter.create();
        OpenTelemetryProxy.build(exporter);
        rootListener = new RootListener();
    }

    @Test
    public void testTerminatedMonitoringEngine() {
        new ChannelKeepAliveSpan().start();
        assert ChannelKeepAliveSpan.current() != null;
        rootListener.onTerminateMonitoringEngine();
        assert ChannelKeepAliveSpan.current() == null;
    }

    @After
    public void tearDown() throws Exception {
        rootListener.onTerminateMonitoringEngine();
        OpenTelemetryProxy.clean();
    }
}
