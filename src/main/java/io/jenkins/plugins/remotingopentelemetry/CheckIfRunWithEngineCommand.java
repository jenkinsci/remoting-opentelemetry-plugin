package io.jenkins.plugins.remotingopentelemetry;

import hudson.remoting.Engine;
import jenkins.security.MasterToSlaveCallable;

/**
 * Sample class
 */
/* package */ final class CheckIfRunWithEngineCommand extends MasterToSlaveCallable<Boolean, RuntimeException> {
    @Override
    public final Boolean call() {
        return Engine.current() != null;
    }
}
