package io.jenkins.plugins.remotingopentelemetry.engine;

import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

public class RemotingMetricExporterProvider {
    public static MetricExporter create(EngineConfiguration config) {
        return OtlpGrpcMetricExporter.builder()
                .setEndpoint(config.getEndpoint())
                .build();
    }
}
