package io.jenkins.plugins.remotingopentelemetry.engine;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;

/**
 * Builds and provides OpenTelemetry global object
 */
public class OpenTelemetryProxy {

    private static final String INSTRUMENTATION_NAME = "jenkins remoting";

    private static Tracer tracer;

    public static void build(SpanProcessor processor, Resource resource) {
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(processor)
                .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .build();

        tracer = openTelemetry.getTracer(INSTRUMENTATION_NAME);
    }

    public static Tracer getTracer() {
        return tracer;
    }

    public static void clean() {
        tracer = null;
    }
}
