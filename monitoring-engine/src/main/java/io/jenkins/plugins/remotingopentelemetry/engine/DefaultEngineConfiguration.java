package io.jenkins.plugins.remotingopentelemetry.engine;

import io.jenkins.plugins.remotingopentelemetry.engine.config.ConfigOption;
import io.jenkins.plugins.remotingopentelemetry.engine.config.Configuration;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.regex.Pattern;

@Configuration
public class DefaultEngineConfiguration implements EngineConfiguration {
    @ConfigOption(env = "OTEL_EXPORTER_OTLP_ENDPOINT", required = true)
    public String endpoint;

    static private final int exporterTimeoutMillis = 30_000;

    @ConfigOption(env = "SERVICE_INSTANCE_ID")
    public String serviceInstanceId = UUID.randomUUID().toString();

    @ConfigOption(env = "REMOTING_OTEL_METRIC_FILTER")
    public Pattern metricsFilterPattern = Pattern.compile(".*");

    public DefaultEngineConfiguration() {}

    @Override
    @Nonnull
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public int getExporterTimeoutMillis() {
        return exporterTimeoutMillis;
    }

    @Override
    @Nonnull
    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    @Override
    @Nonnull
    public Pattern getMetricsFilterPattern() {
        return metricsFilterPattern;
    }
}
