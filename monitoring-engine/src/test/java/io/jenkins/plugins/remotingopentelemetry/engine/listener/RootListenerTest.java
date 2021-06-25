package io.jenkins.plugins.remotingopentelemetry.engine.listener;

import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.jenkins.plugins.remotingopentelemetry.engine.RemotingResourceProvider;
import io.jenkins.plugins.remotingopentelemetry.engine.span.ChannelInitializationSpan;
import io.jenkins.plugins.remotingopentelemetry.engine.span.JnlpEndpointResolvingSpan;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

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
        RemoteEngineListener listener = new RemoteEngineListener();
        listener.status("Locating server among [localhost]");
        Assert.assertNotNull(ChannelInitializationSpan.current());
        Assert.assertNotNull(JnlpEndpointResolvingSpan.current());
        rootListener.onTerminateMonitoringEngine();
        Assert.assertNull(ChannelInitializationSpan.current());
        Assert.assertNull(JnlpEndpointResolvingSpan.current());
    }

    @After
    public void tearDown() throws Exception {
        rootListener.onTerminateMonitoringEngine();
        OpenTelemetryProxy.clean();
    }
}
