package io.jenkins.plugins.remotingopentelemetry.engine.listener;

import io.jenkins.plugins.remotingopentelemetry.engine.EngineConfiguration;
import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.jenkins.plugins.remotingopentelemetry.engine.RemotingResourceProvider;
import io.jenkins.plugins.remotingopentelemetry.engine.span.ChannelInitializationSpan;
import io.jenkins.plugins.remotingopentelemetry.engine.span.JnlpEndpointResolvingSpan;
import io.jenkins.plugins.remotingopentelemetry.engine.span.JnlpInitializationSpan;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.List;

public class RemoteEngineListenerTest {
    InMemorySpanExporter spanExporter;
    InMemoryMetricExporter metricExporter;

    @Before
    public void setup() throws Exception {
        EngineConfiguration config = new EngineConfiguration("http://localhost", "test");
        spanExporter = InMemorySpanExporter.create();
        SpanProcessor spanProcessor = SimpleSpanProcessor.create(spanExporter);
        Resource resource = RemotingResourceProvider.create(config);
        metricExporter = InMemoryMetricExporter.create();
        OpenTelemetryProxy.build(spanProcessor, metricExporter, resource, config);
    }

    @Test
    public void testLocatingServer() throws Exception {
        RemoteEngineListener listener = new RemoteEngineListener();
        Assert.assertNull(ChannelInitializationSpan.current());
        Assert.assertNull(JnlpEndpointResolvingSpan.current());
        listener.status("Locating server among [localhost]");
        Assert.assertNotNull(ChannelInitializationSpan.current());
        Assert.assertNotNull(JnlpEndpointResolvingSpan.current());
    }

    @Test
    public void testCouldNotResolveJnlpAgentEndpoint() throws Exception {
        RemoteEngineListener listener = new RemoteEngineListener();
        Assert.assertEquals(0, spanExporter.getFinishedSpanItems().size());
        listener.status("Locating server among [localhost]");
        listener.status("Could not resolve JNLP agent endpoint");
        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        Assert.assertEquals(2, spans.size()); // ChannelInitializationSpan and ResolveJnlpEndpointSpan
    }

    @Test
    public void testCouldNotResolveServverAmong() throws Exception {
        RemoteEngineListener listener = new RemoteEngineListener();
        Assert.assertEquals(0, spanExporter.getFinishedSpanItems().size());
        listener.status("Locating server among [localhost]");
        listener.status("Could not resolve server among [localhost]");
        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        Assert.assertEquals(2, spans.size()); // ChannelInitializationSpan and ResolveJnlpEndpointSpan
    }

    @Test
    public void testAgentDiscoverySuccessful() throws Exception {
        RemoteEngineListener listener = new RemoteEngineListener();
        Assert.assertEquals(0, spanExporter.getFinishedSpanItems().size());
        listener.status("Locating server among [localhost]");
        listener.status(String.format("Agent discovery successful%n"
                + "  Agent address: %s%n"
                + "  Agent port:    %d%n"
                + "  Identity:      %s%n",
                "localhost", 1234, "01:23:45:67:89:ab:cd:ef:01:23:45:67:89:ab:cd:ef"
        ));
        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        Assert.assertEquals(1, spans.size()); // ResolveJnlpEndpointSpan
    }

    @Test
    public void testHandshaking() throws Exception {
        RemoteEngineListener listener = new RemoteEngineListener();
        listener.status("Locating server among [localhost]");
        listener.status(String.format("Agent discovery successful%n"
                        + "  Agent address: %s%n"
                        + "  Agent port:    %d%n"
                        + "  Identity:      %s%n",
                "localhost", 1234, "01:23:45:67:89:ab:cd:ef:01:23:45:67:89:ab:cd:ef"
        ));
        Assert.assertEquals(1, spanExporter.getFinishedSpanItems().size()); // ChannelInitializationSpan
        listener.status("Handshaking");
        Assert.assertNotNull(JnlpInitializationSpan.current());
    }

    @Test
    public void testProtocolIsNotEnabled() throws Exception {
        RemoteEngineListener listener = new RemoteEngineListener();
        listener.status("Locating server among [localhost]");
        listener.status(String.format("Agent discovery successful%n"
                        + "  Agent address: %s%n"
                        + "  Agent port:    %d%n"
                        + "  Identity:      %s%n",
                "localhost", 1234, "01:23:45:67:89:ab:cd:ef:01:23:45:67:89:ab:cd:ef"
        ));
        listener.status("Handshaking");
        JnlpInitializationSpan jnlpInitializationSpan = JnlpInitializationSpan.current();
        listener.status("Protocol test-protocol is not enabled, skipping");

        Assert.assertEquals(2, spanExporter.getFinishedSpanItems().size()); // ChannelInitializationSpan and JNLPInitializationSpan
        Assert.assertNotNull(ChannelInitializationSpan.current());
        Assert.assertEquals("test-protocol", jnlpInitializationSpan.getProtocolName());
    }

    @Test
    public void testServerReportsProtocolNotSupported() throws Exception {
        RemoteEngineListener listener = new RemoteEngineListener();
        listener.status("Locating server among [localhost]");
        listener.status(String.format("Agent discovery successful%n"
                        + "  Agent address: %s%n"
                        + "  Agent port:    %d%n"
                        + "  Identity:      %s%n",
                "localhost", 1234, "01:23:45:67:89:ab:cd:ef:01:23:45:67:89:ab:cd:ef"
        ));
        listener.status("Handshaking");
        JnlpInitializationSpan jnlpInitializationSpan = JnlpInitializationSpan.current();
        listener.status("Server reports protocol test-protocol not supported, skipping");

        Assert.assertEquals(2, spanExporter.getFinishedSpanItems().size()); // ChannelInitializationSpan and JNLPInitializationSpan
        Assert.assertEquals("test-protocol", jnlpInitializationSpan.getProtocolName());
        Assert.assertNotNull(ChannelInitializationSpan.current());
    }

    @Test
    public void testTryingProtocol() throws Exception {
        RemoteEngineListener listener = new RemoteEngineListener();
        listener.status("Locating server among [localhost]");
        listener.status(String.format("Agent discovery successful%n"
                        + "  Agent address: %s%n"
                        + "  Agent port:    %d%n"
                        + "  Identity:      %s%n",
                "localhost", 1234, "01:23:45:67:89:ab:cd:ef:01:23:45:67:89:ab:cd:ef"
        ));
        listener.status("Handshaking");
        listener.status("Trying protocol: test-protocol");

        Assert.assertEquals(1, spanExporter.getFinishedSpanItems().size()); // ChannelInitializationSpan
        Assert.assertEquals("test-protocol", JnlpInitializationSpan.current().getProtocolName());
    }

    @Test
    public void testProtocolConnectionError() throws Exception {
        RemoteEngineListener listener = new RemoteEngineListener();
        listener.status("Locating server among [localhost]");
        listener.status(String.format("Agent discovery successful%n"
                        + "  Agent address: %s%n"
                        + "  Agent port:    %d%n"
                        + "  Identity:      %s%n",
                "localhost", 1234, "01:23:45:67:89:ab:cd:ef:01:23:45:67:89:ab:cd:ef"
        ));
        listener.status("Handshaking");
        listener.status("Trying protocol: test-protocol");
        listener.status("Protocol test-protocol failed to establish channel", new Exception("Test Exception"));
        Assert.assertEquals(2, spanExporter.getFinishedSpanItems().size()); // ChannelInitializationSpan and JNLPInitializationSpan
        Assert.assertNotNull(ChannelInitializationSpan.current());
    }

    @Test
    public void testSecondJnlpProtocolTry() throws Exception {
        RemoteEngineListener listener = new RemoteEngineListener();
        listener.status("Locating server among [localhost]");
        listener.status(String.format("Agent discovery successful%n"
                        + "  Agent address: %s%n"
                        + "  Agent port:    %d%n"
                        + "  Identity:      %s%n",
                "localhost", 1234, "01:23:45:67:89:ab:cd:ef:01:23:45:67:89:ab:cd:ef"
        ));
        listener.status("Handshaking");
        listener.status("Trying protocol: test-protocol-1");
        listener.status("Protocol test-protocol-1 failed to establish channel", new Exception("Test Exception"));
        listener.status("Trying protocol: test-protocol-2");
        listener.status("Protocol test-protocol-2 failed to establish channel", new Exception("Test Exception"));
        Assert.assertEquals(3, spanExporter.getFinishedSpanItems().size()); // ChannelInitializationSpan and JNLPInitializationSpan x 2
        Assert.assertNotNull(ChannelInitializationSpan.current());
    }

    @Test
    public void testAllJNLPProtocolFailure() throws Exception {
        RemoteEngineListener listener = new RemoteEngineListener();
        listener.status("Locating server among [localhost]");
        listener.status(String.format("Agent discovery successful%n"
                        + "  Agent address: %s%n"
                        + "  Agent port:    %d%n"
                        + "  Identity:      %s%n",
                "localhost", 1234, "01:23:45:67:89:ab:cd:ef:01:23:45:67:89:ab:cd:ef"
        ));
        listener.status("Handshaking");
        listener.status("Trying protocol: test-protocol-1");
        listener.status("Protocol test-protocol-1 failed to establish channel", new Exception("Test Exception"));
        listener.status("Trying protocol: test-protocol-2");
        listener.status("Protocol test-protocol-2 failed to establish channel", new Exception("Test Exception"));
        listener.error(new Exception("The server rejected the connection: None of the protocols were accepted"));

        // ChannelInitializationSpan, JNLPInitializationSpan x 2 and ChannelInitializationSpan
        Assert.assertEquals(4, spanExporter.getFinishedSpanItems().size());
        Assert.assertNull(ChannelInitializationSpan.current());
    }

    @Test
    public void testNoEnabledProtocols() throws Exception {
        RemoteEngineListener listener = new RemoteEngineListener();
        listener.status("Locating server among [localhost]");
        listener.status(String.format("Agent discovery successful%n"
                        + "  Agent address: %s%n"
                        + "  Agent port:    %d%n"
                        + "  Identity:      %s%n",
                "localhost", 1234, "01:23:45:67:89:ab:cd:ef:01:23:45:67:89:ab:cd:ef"
        ));
        listener.status("Handshaking");
        listener.status("Protocol test-protocol-1 is not enabled, skipping");
        listener.status("Server reports protocol test-protocol-2 not supported, skipping");
        listener.error(new Exception("The server rejected the connection: None of the protocols are enabled"));

        // ChannelInitializationSpan, JNLPInitializationSpan x 2 and ChannelInitializationSpan
        Assert.assertEquals(4, spanExporter.getFinishedSpanItems().size());
        Assert.assertNull(ChannelInitializationSpan.current());
    }

    @Test
    public void testConnected() throws Exception {
        RemoteEngineListener listener = new RemoteEngineListener();
        Assert.assertEquals(0, spanExporter.getFinishedSpanItems().size());
        listener.status("Locating server among [localhost]");
        listener.status(String.format("Agent discovery successful%n"
                        + "  Agent address: %s%n"
                        + "  Agent port:    %d%n"
                        + "  Identity:      %s%n",
                "localhost", 1234, "01:23:45:67:89:ab:cd:ef:01:23:45:67:89:ab:cd:ef"
        ));
        Assert.assertEquals(1, spanExporter.getFinishedSpanItems().size()); // ResolveJnlpEndpointSpan
        listener.status("Connected");
        Assert.assertEquals(2, spanExporter.getFinishedSpanItems().size()); // ChannelInitializationSpan and ResolveJnlpEndpointSpan
    }

    @After
    public void tearDown() throws Exception {
        OpenTelemetryProxy.clean();
        spanExporter.reset();
        metricExporter.reset();
        new RootListener().onTerminateMonitoringEngine();
    }
}
