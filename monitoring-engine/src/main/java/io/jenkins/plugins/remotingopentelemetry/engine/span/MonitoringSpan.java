package io.jenkins.plugins.remotingopentelemetry.engine.span;

import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

import javax.annotation.Nullable;
import java.util.function.Function;

/*package*/abstract class MonitoringSpan {
    abstract protected String getSpanName();

    private Function<Span, Void> setAttributeChain = (Span span) -> { return null; };

    @Nullable
    protected final Tracer tracer;

    @Nullable
    protected Span span;

    public MonitoringSpan() {
        tracer = OpenTelemetryProxy.getTracer();
    }

    public MonitoringSpan start() {
        if (tracer == null) return this;
        span = tracer.spanBuilder(getSpanName()).startSpan();
        setAttributeChain.apply(span);
        return this;
    }

    public void end() {
        if (span != null) span.end();
    }

    protected MonitoringSpan setAttribute(String key, String value) {
        if (span != null) span.setAttribute(key, value);
        Function<Span, Void> innerFunc = setAttributeChain;
        setAttributeChain = (Span span) -> {
            span.setAttribute(key, value);
            return innerFunc.apply(span);
        };
        return this;
    }
    protected MonitoringSpan setAttribute(String key, long value) {
        if (span != null) span.setAttribute(key, value);
        Function<Span, Void> innerFunc = setAttributeChain;
        setAttributeChain = (Span span) -> {
            span.setAttribute(key, value);
            return innerFunc.apply(span);
        };
        return this;
    }
    protected MonitoringSpan setAttribute(String key, double value) {
        if (span != null) span.setAttribute(key, value);
        Function<Span, Void> innerFunc = setAttributeChain;
        setAttributeChain = (Span span) -> {
            span.setAttribute(key, value);
            return innerFunc.apply(span);
        };
        return this;
    }
    protected MonitoringSpan setAttribute(String key, boolean value) {
        if (span != null) span.setAttribute(key, value);
        Function<Span, Void> innerFunc = setAttributeChain;
        setAttributeChain = (Span span) -> {
            span.setAttribute(key, value);
            return innerFunc.apply(span);
        };
        return this;
    }
}
