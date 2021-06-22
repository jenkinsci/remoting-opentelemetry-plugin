package io.jenkins.plugins.remotingopentelemetry.commands;

import hudson.slaves.DumbSlave;
import io.jenkins.plugins.remotingopentelemetry.engine.EngineConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class UpdateMonitoringEngineConfigCommandTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void test() throws Exception {
        DumbSlave n = j.createOnlineSlave();
        EngineConfiguration config = new EngineConfiguration("http://localhost");
        n.getChannel().call(new UpdateMonitoringEngineConfigCommand(config));
    }
}
