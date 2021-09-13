package io.jenkins.plugins.remotingopentelemetry.engine;

import io.jenkins.plugins.remotingopentelemetry.engine.log.BatchLogProcessor;
import io.jenkins.plugins.remotingopentelemetry.engine.log.LogExporter;
import io.jenkins.plugins.remotingopentelemetry.engine.log.LogProcessor;
import io.jenkins.plugins.remotingopentelemetry.engine.metric.FilesystemMetric;
import io.jenkins.plugins.remotingopentelemetry.engine.metric.GarbageCollectorMXBeanMetric;
import io.jenkins.plugins.remotingopentelemetry.engine.metric.MemoryMXBeanMetric;
import io.jenkins.plugins.remotingopentelemetry.engine.metric.MemoryPoolMXBeanMetric;
import io.jenkins.plugins.remotingopentelemetry.engine.metric.OperatingSystemMXBeanMetric;
import io.jenkins.plugins.remotingopentelemetry.engine.metric.ConnectionEstablishmentsCountMetric;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;

import java.util.regex.Pattern;

public final class MonitoringEngine {
    EngineConfiguration  config;

    public MonitoringEngine (EngineConfiguration config) {
        this.config = config;

        MetricExporter metricExporter = RemotingMetricExporterProvider.create(config);
        Resource resource = RemotingResourceProvider.create(config);
        LogExporter logExporter = RemotingLogExporterProvider.create(config);
        LogProcessor logProcessor = BatchLogProcessor.builder(logExporter).build();
        OpenTelemetryProxy.build(metricExporter, logProcessor, resource, config);
    }

    public void start() {
        SdkMeterProvider sdkMeterProvider = OpenTelemetryProxy.getSdkMeterProvider();

        if (sdkMeterProvider != null) {
            setupMetrics(sdkMeterProvider);
        }
    }

    private void setupMetrics(SdkMeterProvider sdkMeterProvider) {
        Pattern metricsFilterPattern = config.getMetricsFilterPattern();
        new OperatingSystemMXBeanMetric(sdkMeterProvider, metricsFilterPattern).register();
        new MemoryMXBeanMetric(sdkMeterProvider, metricsFilterPattern).register();
        new MemoryPoolMXBeanMetric(sdkMeterProvider, metricsFilterPattern).register();
        new GarbageCollectorMXBeanMetric(sdkMeterProvider, metricsFilterPattern).register();
        new FilesystemMetric(sdkMeterProvider, metricsFilterPattern).register();
        new ConnectionEstablishmentsCountMetric(sdkMeterProvider, metricsFilterPattern).register();

        OpenTelemetryProxy.startIntervalMetricReader();
    }
}
