package io.jenkins.plugins.remotingopentelemetry.engine;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class OpenTelemetryProxy {
    private static final String INSTRUMENTATION_NAME = "jenkins remoting";
    private static Tracer tracer;

    public static void build() {
         build(new LoggingSpanExporter());
    }

    public static void build(SpanExporter exporter) {
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .build();

        tracer = openTelemetry.getTracer(INSTRUMENTATION_NAME);
    }

    public static Tracer getTracer() {
        return tracer;
    }

    public static void clean() {
        tracer = null;
    }
}
