package io.jenkins.plugins.remotingopentelemetry.engine;

import java.lang.instrument.Instrumentation;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PreMain {
    static final Logger LOGGER = Logger.getLogger(PreMain.class.getName());

    public static void premain(String agentArgs, Instrumentation inst) {
        String endpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT");
        if (endpoint == null) {
            LOGGER.log(Level.WARNING, "OpenTelemetry endpoint is not set");
            return;
        }

        String metricsFilterPatternString = System.getenv("REMOTING_OTEL_METRIC_FILTER");
        Pattern metricsFilterPattern;
        if (metricsFilterPatternString == null) metricsFilterPatternString = ".*";
        try {
            metricsFilterPattern = Pattern.compile(metricsFilterPatternString);
        } catch (PatternSyntaxException e) {
            LOGGER.log(Level.WARNING, "Metric filter regex is invalid: ({0}). will use \".*\" instead.", metricsFilterPatternString);
            metricsFilterPattern = Pattern.compile(".*");
        }

        String serviceInstanceId = System.getenv("SERVICE_INSTANCE_ID");
        if (serviceInstanceId == null) {
            serviceInstanceId = UUID.randomUUID().toString();
            LOGGER.log(Level.INFO, "Service instance id is not set. will use {0} instead.", serviceInstanceId);
        }

        EngineConfiguration config = new EngineConfiguration(
                endpoint, serviceInstanceId, metricsFilterPattern
        );

        MonitoringEngine engine = new MonitoringEngine(config);
        engine.start();
    }
}
