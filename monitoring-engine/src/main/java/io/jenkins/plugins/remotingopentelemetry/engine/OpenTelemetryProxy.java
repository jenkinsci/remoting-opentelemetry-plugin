package io.jenkins.plugins.remotingopentelemetry.engine;

import io.jenkins.plugins.remotingopentelemetry.engine.log.LogProcessor;
import io.jenkins.plugins.remotingopentelemetry.engine.log.LogSinkSdkProvider;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.logging.LogSink;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;

import javax.annotation.Nullable;
import java.util.Collections;

/**
 * Builds and provides OpenTelemetry global object
 */
public class OpenTelemetryProxy {

    @Nullable
    private static final String INSTRUMENTATION_NAME = "jenkins remoting";

    @Nullable
    private static SdkMeterProvider sdkMeterProvider;

    @Nullable
    private static IntervalMetricReader intervalMetricReader;

    @Nullable
    private static LogSinkSdkProvider logSinkSdkProvider;

    public static void build(
            @Nullable MetricExporter  metricExporter,
            @Nullable LogProcessor logProcessor,
            Resource resource,
            EngineConfiguration config) {

        if (metricExporter != null) {
            sdkMeterProvider = SdkMeterProvider.builder()
                    .setResource(resource)
                    .build();

            intervalMetricReader = IntervalMetricReader.builder()
                    .setMetricExporter(metricExporter)
                    .setMetricProducers(Collections.singleton(sdkMeterProvider))
                    .setExportIntervalMillis(config.getExporterTimeoutMillis())
                    .build();
        }

        if (logProcessor != null) {
            logSinkSdkProvider = LogSinkSdkProvider.builder().setResource(resource).build();

            logSinkSdkProvider.addLogProcessor(logProcessor);
        }
    }

    @Nullable
    public static Meter getMeter(String instrumentation) {
        if (sdkMeterProvider == null) return null;
        return sdkMeterProvider.get(instrumentation);
    }

    @Nullable
    public static SdkMeterProvider getSdkMeterProvider() {
        return sdkMeterProvider;
    }

    @Nullable
    public static LogSink getLogSink() {
        if(logSinkSdkProvider == null) return null;
        return logSinkSdkProvider.get(INSTRUMENTATION_NAME);
    }

    @Nullable
    public static void startIntervalMetricReader() {
        if (intervalMetricReader == null) return;
        intervalMetricReader.start();
    }

    public static void clean() {
        if (intervalMetricReader != null) intervalMetricReader.shutdown();
        if (logSinkSdkProvider != null) {
            logSinkSdkProvider.forceFlush();
            logSinkSdkProvider.shutdown();
        }
    }
}
