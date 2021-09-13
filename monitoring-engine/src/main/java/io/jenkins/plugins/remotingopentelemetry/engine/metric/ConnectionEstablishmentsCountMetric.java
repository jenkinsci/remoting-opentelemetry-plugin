package io.jenkins.plugins.remotingopentelemetry.engine.metric;

import io.jenkins.plugins.remotingopentelemetry.engine.semconv.OpenTelemetryMetricsSemanticConventions;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

public class ConnectionEstablishmentsCountMetric {
    private static LongCounter counter = null;

    @Nullable
    public static LongCounter getCounter() {
        return counter;
    }

    private static void setGlobalCounter(LongCounter counter) {
        ConnectionEstablishmentsCountMetric.counter = counter;
    }

    private final Meter meter;
    private final Pattern filterPattern;

    public ConnectionEstablishmentsCountMetric(SdkMeterProvider sdkMeterProvider, Pattern filterPattern) {
        meter = sdkMeterProvider.get(ConnectionEstablishmentsCountMetric.class.getName());
        this.filterPattern = filterPattern;
    }

    public void register() {
        if (filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.REMOTING_CONNECTION_ESTABLISHMENTS_COUNT).matches()) {
            LongCounter counter = meter.longCounterBuilder(OpenTelemetryMetricsSemanticConventions.REMOTING_CONNECTION_ESTABLISHMENTS_COUNT)
                    .setDescription("The number of reconnection")
                    .setUnit("collections")
                    .build();
            setGlobalCounter(counter);
        }
    }
}
