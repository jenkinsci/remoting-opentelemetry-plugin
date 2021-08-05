package io.jenkins.plugins.remotingopentelemetry.engine;

import io.jenkins.plugins.remotingopentelemetry.engine.log.LogExporter;
import io.jenkins.plugins.remotingopentelemetry.engine.log.OtlpGrpcLogExporter;

public class RemotingLogExporterProvider {
    public static LogExporter create(EngineConfiguration config) {
        return OtlpGrpcLogExporter.builder().setEndpoint(config.getEndpoint()).build();
    };
}
