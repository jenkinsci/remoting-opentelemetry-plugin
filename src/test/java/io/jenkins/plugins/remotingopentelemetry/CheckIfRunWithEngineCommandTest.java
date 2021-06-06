package io.jenkins.plugins.remotingopentelemetry;

import hudson.slaves.DumbSlave;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class CheckIfRunWithEngineCommandTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testHelloWorld() throws Exception {
        DumbSlave node = j.createOnlineSlave();
        assert !node.getChannel().call(new CheckIfRunWithEngineCommand());
    }
}
