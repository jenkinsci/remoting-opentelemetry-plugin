package io.jenkins.plugins.remotingopentelemetry.engine.span;

import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChannelKeepAliveSpanTest {
    @Before
    public void setup() throws Exception {
        InMemorySpanExporter exporter = InMemorySpanExporter.create();
        OpenTelemetryProxy.build(exporter);
        ChannelKeepAliveSpan span = ChannelKeepAliveSpan.current();
        if (span != null) {
            span.end();
        }
    }

    @Test
    public void testStart() throws Exception {
        assert ChannelKeepAliveSpan.current() == null;
        new ChannelKeepAliveSpan().start();
        assert ChannelKeepAliveSpan.current() != null;
        new ChannelKeepAliveSpan().end();
    }

    @Test
    public void testEnd() throws Exception {
        new ChannelKeepAliveSpan().start();
        assert ChannelKeepAliveSpan.current() != null;
        new ChannelKeepAliveSpan().end();
        assert ChannelKeepAliveSpan.current() == null;
    }

    @After
    public void tearDown() throws Exception {
        OpenTelemetryProxy.clean();
    }
}
