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
    private String nodeName = "";

    public EngineConfiguration(String endpoint, String nodeName) {
        this.endpoint = endpoint;
        this.nodeName = nodeName;
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
}
