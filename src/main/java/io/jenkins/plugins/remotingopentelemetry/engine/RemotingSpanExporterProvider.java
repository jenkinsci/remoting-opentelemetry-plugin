package io.jenkins.plugins.remotingopentelemetry.engine;

import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;

import javax.annotation.Nonnull;

/**
 * Provides {@link SpanExporter}
 */
public class RemotingSpanExporterProvider {
    /**
     * @param config to create {@link SpanExporter}
     * @return configured {@link SpanExporter}
     */
    @Nonnull
    public static SpanExporter create(EngineConfiguration config) {
        // TODO: Enable to configure timeout
        return OtlpGrpcSpanExporter.builder()
                .setEndpoint(config.getEndpoint())
                .build();
    }
}
