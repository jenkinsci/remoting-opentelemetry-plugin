package io.jenkins.plugins.remotingopentelemetry.engine.span;

import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.jenkins.plugins.remotingopentelemetry.engine.RemotingResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.Assert;
import org.junit.Test;

public class JnlpInitializationSpanTest {
    @Test
    public void testSetProtocolName() throws Exception {
        InMemorySpanExporter exporter = InMemorySpanExporter.create();
        Resource resource = RemotingResourceProvider.create();
        OpenTelemetryProxy.build(SimpleSpanProcessor.create(exporter), resource);

        ChannelInitializationSpan channelInitializationSpan = new ChannelInitializationSpan();
        channelInitializationSpan.start();

        JnlpInitializationSpan jnlpInitializationSpan = new JnlpInitializationSpan();
        jnlpInitializationSpan.start(channelInitializationSpan);

        jnlpInitializationSpan.setProtocolName("New Protocol");

        Assert.assertEquals("New Protocol", JnlpInitializationSpan.current().getProtocolName());
    }
}
