package io.jenkins.plugins.remotingopentelemetry.engine.span;

import io.jenkins.plugins.remotingopentelemetry.engine.EngineConfiguration;
import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.jenkins.plugins.remotingopentelemetry.engine.RemotingResourceProvider;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.Assert;
import org.junit.Test;

public class JnlpInitializationSpanTest {
    @Test
    public void testSetProtocolName() throws Exception {
        EngineConfiguration config = new EngineConfiguration("http://localhost", "test");
        InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
        InMemoryMetricExporter metricExporter = InMemoryMetricExporter.create();
        SpanProcessor spanProcessor = SimpleSpanProcessor.create(spanExporter);
        Resource resource = RemotingResourceProvider.create(config);
        OpenTelemetryProxy.build(spanProcessor, metricExporter, resource, config);

        ChannelInitializationSpan channelInitializationSpan = new ChannelInitializationSpan();
        channelInitializationSpan.start();

        JnlpInitializationSpan jnlpInitializationSpan = new JnlpInitializationSpan();
        jnlpInitializationSpan.start(channelInitializationSpan);

        jnlpInitializationSpan.setProtocolName("New Protocol");

        Assert.assertEquals("New Protocol", JnlpInitializationSpan.current().getProtocolName());

        spanExporter.reset();
        metricExporter.reset();
    }
}
