package io.jenkins.plugins.remotingopentelemetry;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.remoting.Which;
import hudson.slaves.ComputerListener;
import io.jenkins.plugins.remotingopentelemetry.commands.SyncMonitoringEngineCommand;
import io.jenkins.plugins.remotingopentelemetry.engine.EngineConfiguration;
import jenkins.model.Jenkins.MasterComputer;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transfers the monitoring engine and the configurations to the online agents
 *
 * @author Akihiro Kiuchi
 */
@Extension
public final class RemotingOpenTelemetryComputerListener extends ComputerListener {
    final static Logger LOGGER = Logger.getLogger(RemotingOpenTelemetryComputerListener.class.getName());

    @Override
    public final void onOnline(Computer c, TaskListener listener) {
        if (c instanceof MasterComputer) return;

        VirtualChannel vc = c.getChannel();
        if (!(vc instanceof Channel)) {
            LOGGER.log(Level.WARNING, "Invalid channel type of " + c.getNode());
            return;
        }

        Channel ch = (Channel) vc;

        EngineConfiguration config = RemotingOpenTelemetryConfiguration.get().export();

        try {
            URL jar = Which.jarFile(EngineConfiguration.class).toURI().toURL();
            ch.preloadJar(getClass().getClassLoader(), jar);
            ch.call(new SyncMonitoringEngineCommand(config));
        } catch (IOException | InterruptedException e) {
            String command = SyncMonitoringEngineCommand.class.getName();
            Node node = c.getNode();
            String nodeName = node == null ? "unknown" : node.getNodeName();
            LOGGER.log(Level.WARNING, "Fail to call " + command + " for " + nodeName , e);
        }
    }
}
