package io.jenkins.plugins.remotingopentelemetry.engine.span;

import hudson.remoting.Channel;
import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Span between the first TCP handshaking begins and {@link Channel} is connected
 */
public class ChannelInitializationSpan implements MonitoringSpan {
    @Nullable
    private static ChannelInitializationSpan currentSpan = null;

    public static final String SPAN_NAME = "Channel Initialization";

    @Nullable
    public static synchronized ChannelInitializationSpan current() {
        return currentSpan;
    }

    private static synchronized void setCurrent(ChannelInitializationSpan span) {
        currentSpan = span;
    }

    @Nonnull
    private final Tracer tracer;

    @Nullable
    private Span span;

    public ChannelInitializationSpan() {
        tracer = OpenTelemetryProxy.getTracer();
    }

    public Context getContext() {
        return Context.current().with(span);
    }

    public void start() {
        ChannelInitializationSpan currentSpan = current();
        if (currentSpan != null) {
            currentSpan.end();
        }

        span = tracer.spanBuilder(SPAN_NAME).startSpan();
        setCurrent(this);
    }

    public ChannelInitializationSpan setMessage(String message) {
        span.setAttribute("message", message);
        return this;
    }

    public ChannelInitializationSpan recordException(Throwable exception) {
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
