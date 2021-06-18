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

public class RemoteEngineListenerTest {
    InMemorySpanExporter exporter;

    @Before
    public void setup() throws Exception {
        exporter = InMemorySpanExporter.create();
        Resource resource = RemotingResourceProvider.create();
        OpenTelemetryProxy.build(SimpleSpanProcessor.create(exporter), resource);
    }

    @Test
    public void testStatusConnected() throws Exception {
        assert ChannelKeepAliveSpan.current() == null;
        RemoteEngineListener listener = new RemoteEngineListener();
        listener.status("Connected");
        assert ChannelKeepAliveSpan.current() != null;
    }

    @Test
    public void testStatusTerminated() throws Exception {
        RemoteEngineListener listener = new RemoteEngineListener();
        listener.status("Connected");
        assert ChannelKeepAliveSpan.current() != null;
        listener.status("Terminated");
        assert ChannelKeepAliveSpan.current() == null;
        assert exporter.getFinishedSpanItems().size() == 1;
        assert exporter.getFinishedSpanItems().get(0).getName().equals(ChannelKeepAliveSpan.SPAN_NAME);
    }

    @After
    public void tearDown() throws Exception {
        OpenTelemetryProxy.clean();
        exporter.reset();
    }
}
