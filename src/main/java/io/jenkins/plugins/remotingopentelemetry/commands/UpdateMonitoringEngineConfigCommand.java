package io.jenkins.plugins.remotingopentelemetry.commands;

import io.jenkins.plugins.remotingopentelemetry.engine.EngineConfiguration;
import io.jenkins.plugins.remotingopentelemetry.engine.MonitoringEngine;
import jenkins.security.MasterToSlaveCallable;

public class UpdateMonitoringEngineConfigCommand extends MasterToSlaveCallable<Void, InterruptedException> {

    private final EngineConfiguration config;

    public UpdateMonitoringEngineConfigCommand(EngineConfiguration config) {
        this.config = config;
    }

    @Override
    public Void call() throws InterruptedException {
        MonitoringEngine.terminate();
        MonitoringEngine.launch(config);
        return null;
    }
}
