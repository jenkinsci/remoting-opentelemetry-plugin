package io.jenkins.plugins.remotingopentelemetry.engine.span;

import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Span to resolve the JNLP endpoint
 */
public class JnlpEndpointResolvingSpan implements MonitoringSpan {
    @Nullable
    private static JnlpEndpointResolvingSpan currentSpan = null;

    public static final String SPAN_NAME = "Resolve JNLP Endpoint";

    @Nullable
    public static JnlpEndpointResolvingSpan current() {
        return currentSpan;
    }

    private static void setCurrent(JnlpEndpointResolvingSpan span) {
        currentSpan = span;
    }

    @Nonnull
    private final Tracer tracer;

    @Nullable
    private Span span;

    public JnlpEndpointResolvingSpan() {
        tracer = OpenTelemetryProxy.getTracer();
    }

    public void start(ChannelInitializationSpan parent) {
        JnlpEndpointResolvingSpan currentSpan = current();
        if (currentSpan != null) {
            currentSpan.end();
        }

        span = tracer.spanBuilder(SPAN_NAME)
                .setParent(parent.getContext())
                .startSpan();
        setCurrent(this);
    }

    public JnlpEndpointResolvingSpan setMessage(String message) {
        span.setAttribute("message", message);
        return this;
    }

    public JnlpEndpointResolvingSpan setEndpointCandidate(String candidate) {
        span.setAttribute("candidates", candidate);
        return this;
    }

    public JnlpEndpointResolvingSpan recordException(Throwable exception) {
        span.recordException(exception);
        return this;
    }

    public void end() {
        if (span != null) {
            span.end();
        }
        setCurrent(null);
    }
}
