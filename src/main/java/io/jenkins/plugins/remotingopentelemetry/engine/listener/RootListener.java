package io.jenkins.plugins.remotingopentelemetry.engine.listener;

import hudson.remoting.Engine;
import io.jenkins.plugins.remotingopentelemetry.engine.span.ChannelKeepAliveSpan;
import io.jenkins.plugins.remotingopentelemetry.engine.MonitoringEngine;

import javax.annotation.Nullable;
import java.util.EventListener;

/**
 * Handles the events from {@link MonitoringEngine} and setups child event listeners.
 */
public class RootListener implements EventListener {
    private final RemoteEngineListener remoteEngineListener = new RemoteEngineListener();

    @Nullable
    private Engine remoteEngine = null;

    public void preStartMonitoringEngine() {
        remoteEngine = Engine.current();
        if (remoteEngine != null) {
            remoteEngine.addListener(remoteEngineListener);

            // Currently, we have no way to catch first connection established event,
            // so instead, we start ChannelKeepAliveSpan at this point.
            new ChannelKeepAliveSpan().start();
        }
    }

    public void onTerminateMonitoringEngine() {
        onTerminateMonitoringEngine(null);
    }
    public void onTerminateMonitoringEngine(Exception e) {
        if (remoteEngine != null) {
            remoteEngine.removeListener(remoteEngineListener);
        }
        ChannelKeepAliveSpan channelKeepAliveSpan = ChannelKeepAliveSpan.current();
        if (channelKeepAliveSpan != null) {
            channelKeepAliveSpan.end();
        }
    }
}
