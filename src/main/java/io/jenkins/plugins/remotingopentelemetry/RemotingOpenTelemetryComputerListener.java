package io.jenkins.plugins.remotingopentelemetry;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;
import jenkins.model.Jenkins.MasterComputer;

import java.io.IOException;

/**
 * Sample extension point
 */
@Extension
public final class RemotingOpenTelemetryComputerListener extends ComputerListener {
    @Override
    public final void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
        if (c instanceof MasterComputer) return;
        if (c.getChannel().call(new CheckIfRunWithEngineCommand())) {
            System.out.println("Node " + c.getNode().getNodeName() + " is run with Engine.");
        }
    }
}
