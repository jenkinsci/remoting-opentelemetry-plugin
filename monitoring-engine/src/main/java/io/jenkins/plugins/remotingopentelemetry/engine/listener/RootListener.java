package io.jenkins.plugins.remotingopentelemetry.engine.listener;

import hudson.remoting.Engine;
import io.jenkins.plugins.remotingopentelemetry.engine.span.ChannelInitializationSpan;
import io.jenkins.plugins.remotingopentelemetry.engine.MonitoringEngine;
import io.jenkins.plugins.remotingopentelemetry.engine.span.JnlpEndpointResolvingSpan;
import io.jenkins.plugins.remotingopentelemetry.engine.span.JnlpInitializationSpan;

import javax.annotation.Nullable;
import java.util.EventListener;

/**
 * Handles the events from {@link MonitoringEngine} and setups child event listeners.
 */
public class RootListener implements EventListener {

    private final RemoteEngineListener remoteEngineListener = new RemoteEngineListener();

    @Nullable
    private Engine remoteEngine = null;

    /**
     * Invoked before running the MonitoringEngine thread
     */
    public void preStartMonitoringEngine() {
        remoteEngine = Engine.current();
        if (remoteEngine != null) {
            remoteEngine.addListener(remoteEngineListener);
        }
    }

    /**
     * Invoked when terminating the MonitoringEngine thread
     */
    public void onTerminateMonitoringEngine() {
        onTerminateMonitoringEngine(null);
    }

    public void onTerminateMonitoringEngine(@Nullable Exception e) {
        if (remoteEngine != null) {
            remoteEngine.removeListener(remoteEngineListener);
        }

        // remove all running spans

        JnlpEndpointResolvingSpan resolveJnlpEndpointSpan = JnlpEndpointResolvingSpan.current();
        if (resolveJnlpEndpointSpan != null) {
            if (e != null) resolveJnlpEndpointSpan.recordException(e);
            resolveJnlpEndpointSpan.end();
        }

        JnlpInitializationSpan jnlpInitializationSpan = JnlpInitializationSpan.current();
        if (jnlpInitializationSpan != null) {
            if (e != null) jnlpInitializationSpan.recordException(e);
            jnlpInitializationSpan.end();
        }

        ChannelInitializationSpan channelInitializationSpan = ChannelInitializationSpan.current();
        if (channelInitializationSpan != null) {
            if (e != null) channelInitializationSpan.recordException(e);
            channelInitializationSpan.end();
        }

    }
}
