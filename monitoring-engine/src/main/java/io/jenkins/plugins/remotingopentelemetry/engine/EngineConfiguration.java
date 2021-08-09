package io.jenkins.plugins.remotingopentelemetry.engine;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Immutable configuration object of the Monitoring Engine
 */
public class EngineConfiguration implements Serializable {

    @Nonnull
    private final String endpoint;

    // TODO: Enable to configure this value
    @Nonnull
    private final int exporterTimeoutMillis = 30_000;

    @Nonnull
    private final String serviceInstanceId;

    @Nonnull
    private final Pattern metricsFilterPattern;

    public EngineConfiguration(
            @Nonnull String endpoint,
            @Nonnull String serviceInstanceId,
            @Nonnull Pattern metricPattern) {
        this.endpoint = endpoint;
        this.serviceInstanceId = serviceInstanceId;
        this.metricsFilterPattern = metricPattern;
    }

    @Nonnull
    public String getEndpoint() {
        return endpoint;
    }

    @Nonnull
    public int getExporterTimeoutMillis() {
        return exporterTimeoutMillis;
    }

    @Nonnull
    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    @Nonnull
    public Pattern getMetricsFilterPattern() {
        return metricsFilterPattern;
    }
}
