package io.jenkins.plugins.remotingopentelemetry;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.remoting.VirtualChannel;
import io.jenkins.plugins.remotingopentelemetry.commands.UpdateMonitoringEngineConfigCommand;
import io.jenkins.plugins.remotingopentelemetry.engine.EngineConfiguration;
import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import jenkins.model.Jenkins.MasterComputer;
import jenkins.tools.ToolConfigurationCategory;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configures this plugin
 */
@Extension
public class RemotingOpenTelemetryConfiguration extends GlobalConfiguration {

    private static final Logger LOGGER = Logger.getLogger(RemotingOpenTelemetryConfiguration.class.getName());

    public static RemotingOpenTelemetryConfiguration get() {
        return ExtensionList.lookupSingleton(RemotingOpenTelemetryConfiguration.class);
    }

    private String endpoint = "";

    public RemotingOpenTelemetryConfiguration() {
        load();
    }

    @Override
    @NonNull
    public ToolConfigurationCategory getCategory() {
        return GlobalConfigurationCategory.get(ToolConfigurationCategory.class);
    }

    public String getEndpoint() {
        return endpoint;
    }

    @DataBoundSetter
    public void setEndpoint(String endpoint) {
        if (this.endpoint.equals(endpoint)) return;
        this.endpoint = endpoint;
        save();
        applyConfiguration();
    }

    public EngineConfiguration export(String nodeName) {
        return new EngineConfiguration(endpoint, nodeName);
    }

    private void applyConfiguration() {
        for (Node n : Jenkins.get().getNodes()) {
            Computer c = n.toComputer();
            if (c == null || c instanceof MasterComputer || c.isOffline()) continue;
            VirtualChannel ch = c.getChannel();
            if (ch == null) continue;
            Node node = c.getNode();
            String nodeName = node == null ? "unknown" : node.getNodeName();
            try {
                ch.call(new UpdateMonitoringEngineConfigCommand(export(nodeName)));
            } catch (InterruptedException | IOException e) {
                String command = UpdateMonitoringEngineConfigCommand.class.getName();
                LOGGER.log(Level.WARNING, "Fail to call " + command + " for " + n.getNodeName(), e);
            }
        }
    }
}
