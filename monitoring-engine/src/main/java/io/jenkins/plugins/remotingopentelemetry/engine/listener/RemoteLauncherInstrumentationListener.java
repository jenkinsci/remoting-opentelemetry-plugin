package io.jenkins.plugins.remotingopentelemetry.engine.listener;

import hudson.remoting.Launcher;
import hudson.remoting.LauncherInstrumentationListenerAdapter;
import hudson.remoting.Util;
import io.jenkins.plugins.remotingopentelemetry.engine.EngineConfiguration;
import io.jenkins.plugins.remotingopentelemetry.engine.MonitoringEngine;
import io.jenkins.plugins.remotingopentelemetry.engine.span.LaunchSpan;

public class RemoteLauncherInstrumentationListener extends LauncherInstrumentationListenerAdapter {
    @Override
    public void onLaunch(Launcher launcher) {
        EngineConfiguration config = new EngineConfiguration(
                System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT"),
                System.getenv("NODE_NAME"),
                true, true, true
        );
        try {
            MonitoringEngine.launch(config);
            new LaunchSpan().setRemotingVersion(Util.getVersion()).start();
        } catch (InterruptedException e) {
            // ignore
        }
    }

    @Override
    public void onRunWithStdinStdout() {
        LaunchSpan launchSpan = LaunchSpan.current();
        if (launchSpan != null) launchSpan.setCommunicationProtocol("Standard in/out");
    }

    @Override
    public void onConnected() {
        LaunchSpan launchSpan = LaunchSpan.current();
        if (launchSpan != null) launchSpan.end();
    }
}
