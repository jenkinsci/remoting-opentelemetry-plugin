package io.jenkins.plugins.remotingopentelemetry.commands;

import io.jenkins.plugins.remotingopentelemetry.engine.EngineConfiguration;
import io.jenkins.plugins.remotingopentelemetry.engine.MonitoringEngine;
import jenkins.security.MasterToSlaveCallable;

/**
 * Synchronizes MonitoringEngine versions and configurations with the controller.
 */
public final class SyncMonitoringEngineCommand extends MasterToSlaveCallable<Void, InterruptedException> {

    private final EngineConfiguration config;

    public SyncMonitoringEngineCommand(EngineConfiguration config) {
        this.config = config;
    }

    @Override
    public Void call() throws InterruptedException {
        // TODO: synchronize the version
        if (!MonitoringEngine.isRunning()) {
            MonitoringEngine.launch(config);
        }
        return null;
    }
}
