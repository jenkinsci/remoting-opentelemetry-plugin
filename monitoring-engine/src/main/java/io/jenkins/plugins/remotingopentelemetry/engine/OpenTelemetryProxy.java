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

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds and provides OpenTelemetry global object
 */
public class OpenTelemetryProxy {
    private static Logger LOGGER = Logger.getLogger(OpenTelemetryProxy.class.getName());

    private static final String INSTRUMENTATION_NAME = "jenkins remoting";

    @Nullable
    private static SdkTracerProvider sdkTracerProvider;

    @Nullable
    private static SdkMeterProvider sdkMeterProvider;

    @Nullable
    private static IntervalMetricReader intervalMetricReader;

    @Nullable
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

    @Nullable
    public static Tracer getTracer() {
        if (tracer == null) {
            LOGGER.log(Level.WARNING, "Failed to obtain tracer. OpenTelemetryProxy is not built yet");
        }
        return tracer;
    }

    @Nullable
    public static Meter getMeter(String instrumentation) {
        if (sdkMeterProvider == null) {
            LOGGER.log(Level.WARNING, "Failed to obtain meter provider. OpenTelemetryProxy is not built yet");
            return null;
        }
        return sdkMeterProvider.get(instrumentation);
    }

    @Nullable
    public static SdkMeterProvider getSdkMeterProvider() {
        if (sdkMeterProvider == null) {
            LOGGER.log(Level.WARNING, "Failed to obtain meter provider. OpenTelemetryProxy is not built yet");
        }
        return sdkMeterProvider;
    }

    public static void startIntervalMetricReader() {
        if (intervalMetricReader != null) intervalMetricReader.start();
        else LOGGER.log(Level.WARNING, "Failed to start interval metric reader. OpenTelemetryProxy is not built yet");
    }

    public static void clean() {
        if (sdkTracerProvider != null) sdkTracerProvider.shutdown();
        if (intervalMetricReader != null) intervalMetricReader.shutdown();
        tracer = null;
    }
}
