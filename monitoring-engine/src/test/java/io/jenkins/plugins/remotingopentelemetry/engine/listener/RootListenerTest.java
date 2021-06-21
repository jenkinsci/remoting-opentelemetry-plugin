package io.jenkins.plugins.remotingopentelemetry.engine.listener;

import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.jenkins.plugins.remotingopentelemetry.engine.RemotingResourceProvider;
import io.jenkins.plugins.remotingopentelemetry.engine.span.ChannelKeepAliveSpan;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RootListenerTest {
    RootListener rootListener;

    @Before
    public void setup() throws Exception {
        InMemorySpanExporter exporter = InMemorySpanExporter.create();
        Resource resource = RemotingResourceProvider.create();
        OpenTelemetryProxy.build(SimpleSpanProcessor.create(exporter), resource);
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
