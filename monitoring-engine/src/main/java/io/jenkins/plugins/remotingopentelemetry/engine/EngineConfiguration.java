package io.jenkins.plugins.remotingopentelemetry.engine;

import javax.annotation.Nonnull;
import java.io.Serializable;

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
    private final String nodeName;

    @Nonnull
    private final boolean systemMetricsGroupEnabled;

    @Nonnull
    private final boolean processMetricsGroupEnabled;

    @Nonnull
    private final boolean jvmMetricsGroupEnabled;

    public EngineConfiguration(String endpoint, String nodeName) {
        this(endpoint, nodeName, false, false, false);
    }

    public EngineConfiguration(
            @Nonnull String endpoint,
            @Nonnull String nodeName,
            boolean systemMetricsGroupEnabled,
            boolean processMetricsGroupEnabled,
            boolean jvmMetricsGroupEnabled) {
        this.endpoint = endpoint;
        this.nodeName = nodeName;
        this.systemMetricsGroupEnabled = systemMetricsGroupEnabled;
        this.processMetricsGroupEnabled = processMetricsGroupEnabled;
        this.jvmMetricsGroupEnabled = jvmMetricsGroupEnabled;
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
    public String getNodeName() {
        return nodeName;
    }

    @Nonnull
    public boolean isSystemMetricsGroupEnabled() {
        return systemMetricsGroupEnabled;
    }

    @Nonnull
    public boolean isProcessMetricsGroupEnabled() {
        return processMetricsGroupEnabled;
    }

    @Nonnull
    public boolean isJvmMetricsGroupEnabled() {
        return jvmMetricsGroupEnabled;
    }
}
