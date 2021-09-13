package io.jenkins.plugins.remotingopentelemetry;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.slaves.ComputerListener;
import io.jenkins.plugins.remotingopentelemetry.commands.CountUpConnectionEstablishment;
import jenkins.model.Jenkins;

import java.io.IOException;

@Extension
public class RemotingOpenTelemetryComputerListener extends ComputerListener {
    @Override
    public void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
        if (c instanceof Jenkins.MasterComputer) return;

        VirtualChannel vc = c.getChannel();
        if (!(vc instanceof Channel)) {
            return;
        }

        Channel ch = (Channel) vc;
        ch.call(new CountUpConnectionEstablishment());
    }
}
