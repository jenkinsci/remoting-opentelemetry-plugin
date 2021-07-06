package io.jenkins.plugins.remotingopentelemetry.engine;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;

import java.util.Collections;

/**
 * Builds and provides OpenTelemetry global object
 */
public class OpenTelemetryProxy {

    private static final String INSTRUMENTATION_NAME = "jenkins remoting";

    private static SdkTracerProvider sdkTracerProvider;

    private static SdkMeterProvider sdkMeterProvider;

    private static IntervalMetricReader intervalMetricReader;

    private static Tracer tracer;

    public static void build(
            SpanProcessor spanProcessor,
            MetricExporter metricExporter,
            Resource resource,
            EngineConfiguration config) {

        sdkMeterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .build();

        sdkTracerProvider = SdkTracerProvider.builder()
               .setResource(resource)
               .addSpanProcessor(spanProcessor)
               .build();

        intervalMetricReader = IntervalMetricReader.builder()
                .setMetricExporter(metricExporter)
                .setMetricProducers(Collections.singleton(sdkMeterProvider))
                .setExportIntervalMillis(config.getExporterTimeoutMillis())
                .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .build();

        tracer = openTelemetry.getTracer(INSTRUMENTATION_NAME);
    }

    public static Tracer getTracer() {
        return tracer;
    }

    public static Meter getMeter(String instrumentation) {
        return sdkMeterProvider.get(instrumentation);
    }

    public static void startIntervalMetricReader() {
        intervalMetricReader.start();
    }

    public static void clean() {
        sdkTracerProvider.shutdown();
        if (intervalMetricReader != null) intervalMetricReader.shutdown();
        tracer = null;
    }
}
