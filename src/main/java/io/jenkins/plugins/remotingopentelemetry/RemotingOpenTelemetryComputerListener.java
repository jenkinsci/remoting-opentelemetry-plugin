package io.jenkins.plugins.remotingopentelemetry;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;
import io.jenkins.plugins.remotingopentelemetry.commands.SyncMonitoringEngineCommand;
import jenkins.model.Jenkins.MasterComputer;

import java.io.IOException;

/**
 * Transfers the monitoring engine and the configurations to the online agents
 *
 * @author Akihiro Kiuchi
 */
@Extension
public final class RemotingOpenTelemetryComputerListener extends ComputerListener {
    @Override
    public final void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
        if (c instanceof MasterComputer) return;
        c.getChannel().call(new SyncMonitoringEngineCommand());
    }
}
