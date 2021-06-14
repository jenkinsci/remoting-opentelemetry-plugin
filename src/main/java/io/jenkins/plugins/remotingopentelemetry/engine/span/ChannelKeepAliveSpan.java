package io.jenkins.plugins.remotingopentelemetry.engine.span;

import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

import javax.annotation.Nullable;

public class ChannelKeepAliveSpan implements MonitoringSpan {
    @Nullable
    private static ChannelKeepAliveSpan currentSpan = null;
    public static final String SPAN_NAME = "channel keep alive";
    @Nullable
    public static synchronized ChannelKeepAliveSpan current() {
        return currentSpan;
    }
    private static synchronized void setCurrent(ChannelKeepAliveSpan span) {
        currentSpan = span;
    }

    private final Tracer tracer;
    private Span span;

    public ChannelKeepAliveSpan() {
        tracer = OpenTelemetryProxy.getTracer();
    }

    public void start() {
        this.span = tracer.spanBuilder(SPAN_NAME).startSpan();
        if (currentSpan != null) {
            currentSpan.end();
        }
        setCurrent(this);
    }

    public void end() {
        if (span != null) {
            span.end();
        }
        setCurrent(null);
    }
}
