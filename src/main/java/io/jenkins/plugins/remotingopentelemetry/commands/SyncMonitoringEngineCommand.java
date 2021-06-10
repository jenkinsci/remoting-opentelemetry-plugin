package io.jenkins.plugins.remotingopentelemetry.commands;

import io.jenkins.plugins.remotingopentelemetry.engine.MonitoringEngine;
import jenkins.security.MasterToSlaveCallable;

/**
 * Synchronizes MonitoringEngine versions and configurations with the controller.
 */
public final class SyncMonitoringEngineCommand extends MasterToSlaveCallable<Void, InterruptedException> {
    @Override
    public Void call() throws InterruptedException {
        // TODO: synchronize versions and configurations
        if (!MonitoringEngine.isRunning()) {
            MonitoringEngine.launch();
        }
        return null;
    }
}
