package io.jenkins.plugins.remotingopentelemetry.engine.span;

import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.jenkins.plugins.remotingopentelemetry.engine.RemotingResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChannelKeepAliveSpanTest {
    InMemorySpanExporter exporter;
    @Before
    public void setup() throws Exception {
        exporter = InMemorySpanExporter.create();
        Resource resource = RemotingResourceProvider.create();
        OpenTelemetryProxy.build(SimpleSpanProcessor.create(exporter), resource);
    }

    @Test
    public void testStart() throws Exception {
        assert ChannelKeepAliveSpan.current() == null;
        new ChannelKeepAliveSpan().start();
        assert ChannelKeepAliveSpan.current() != null;
        ChannelKeepAliveSpan.current().end();
    }

    @Test
    public void testEnd() throws Exception {
        new ChannelKeepAliveSpan().start();
        assert ChannelKeepAliveSpan.current() != null;
        ChannelKeepAliveSpan.current().end();
        assert ChannelKeepAliveSpan.current() == null;
        assert exporter.getFinishedSpanItems().size() == 1;
        assert exporter.getFinishedSpanItems().get(0).getName().equals("Channel Keep-Alive");
    }

    @After
    public void tearDown() throws Exception {
        OpenTelemetryProxy.clean();
        exporter.reset();
        ChannelKeepAliveSpan span = ChannelKeepAliveSpan.current();
        if (span != null) {
            span.end();
        }
    }
}
