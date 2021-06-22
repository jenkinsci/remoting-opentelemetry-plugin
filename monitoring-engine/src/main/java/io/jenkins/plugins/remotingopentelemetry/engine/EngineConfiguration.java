package io.jenkins.plugins.remotingopentelemetry.engine;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Immutable configuration object of the Monitoring Engine
 */
public class EngineConfiguration implements Serializable {

    @Nonnull
    private final String endpoint;

    public EngineConfiguration(String endpoint) {
        this.endpoint = endpoint;
    }

    @Nonnull
    public String getEndpoint() {
        return endpoint;
    }
}
