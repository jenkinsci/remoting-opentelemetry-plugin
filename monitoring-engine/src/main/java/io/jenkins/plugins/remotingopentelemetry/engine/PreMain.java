package io.jenkins.plugins.remotingopentelemetry.engine;

import java.lang.instrument.Instrumentation;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreMain {
    static final Logger LOGGER = Logger.getLogger(PreMain.class.getName());

    public static void premain(String agentArgs, Instrumentation inst) {
        String endpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT");
        if (endpoint == null) {
            LOGGER.log(Level.WARNING, "OpenTelemetry endpoint is not set");
            return;
        }

        String serviceInstanceId = System.getenv("SERVICE_INSTANCE_ID");
        if (serviceInstanceId == null) {
            serviceInstanceId = UUID.randomUUID().toString();
            LOGGER.log(Level.INFO, "service instance id is not set will use {0} instead", serviceInstanceId);
        }

        EngineConfiguration config = new EngineConfiguration(
                endpoint, serviceInstanceId, true, true, true
        );

        MonitoringEngine engine = new MonitoringEngine(config);
        engine.start();
    }
}
