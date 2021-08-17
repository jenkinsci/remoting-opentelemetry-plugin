package io.jenkins.plugins.remotingopentelemetry.engine;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.regex.Pattern;

public class TestEngineConfiguration implements EngineConfiguration {
    public String endpoint = "http://localhost";
    public int timeoutMillis = 30_000;
    public String serviceInstanceId = "test";
    public Pattern metricsFilterPattern = Pattern.compile(".*");

    @NonNull
    @Override
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public int getExporterTimeoutMillis() {
        return timeoutMillis;
    }

    @NonNull
    @Override
    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    @NonNull
    @Override
    public Pattern getMetricsFilterPattern() {
        return metricsFilterPattern;
    }
}
