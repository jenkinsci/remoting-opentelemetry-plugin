package io.jenkins.plugins.remotingopentelemetry.engine.span;

import hudson.remoting.Channel;
import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Span between the JNLP starts adn {@link Channel} is connected or the JNLP fails
 */
public class JnlpInitializationSpan implements MonitoringSpan {
    @Nullable
    private static JnlpInitializationSpan currentSpan = null;

    public static final String SPAN_NAME = "JNLP Initialization";

    @Nullable
    public static JnlpInitializationSpan current() {
        return currentSpan;
    }

    private static void setCurrent(JnlpInitializationSpan span) {
        currentSpan = span;
    }

    @Nonnull
    private final Tracer tracer;

    @Nullable
    private Span span;

    @Nullable
    private String protocolName;

    public JnlpInitializationSpan() {
        tracer = OpenTelemetryProxy.getTracer();
    }

    public void start(ChannelInitializationSpan parent) {
        JnlpInitializationSpan currentSpan = current();
        if (currentSpan != null) {
            currentSpan.end();
        }

        span = tracer.spanBuilder(SPAN_NAME)
                .setParent(parent.getContext())
                .startSpan();

        setCurrent(this);
    }

    public Context getContext() {
        return Context.current().with(span);
    }

    public JnlpInitializationSpan setProtocolName(String protocolName) {
        this.protocolName = protocolName;
        span.setAttribute("protocol", protocolName);
        return this;
    }

    public String getProtocolName() {
        return this.protocolName;
    }

    public JnlpInitializationSpan setMessage(String message) {
        span.setAttribute("message", message);
        return this;
    }

    public JnlpInitializationSpan recordException(Throwable exception) {
        span.recordException(exception);
        return this;
    }

    public void end() {
        if (span != null) {
            span.end();
        }
        setCurrent(null);;
    }

    public void abort() {
        setCurrent(null);
    }
}
