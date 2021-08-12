package io.jenkins.plugins.remotingopentelemetry.engine;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public interface EngineConfiguration {

    @Nonnull
    String getEndpoint();

    int getExporterTimeoutMillis();

    @Nonnull
    String getServiceInstanceId();

    @Nonnull
    Pattern getMetricsFilterPattern();
}
