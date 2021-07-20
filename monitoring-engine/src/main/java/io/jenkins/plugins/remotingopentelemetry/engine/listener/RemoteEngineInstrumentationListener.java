package io.jenkins.plugins.remotingopentelemetry.engine.listener;

import hudson.remoting.Engine;
import hudson.remoting.EngineInstrumentationListenerAdapter;
import io.jenkins.plugins.remotingopentelemetry.engine.span.LaunchSpan;

public class RemoteEngineInstrumentationListener extends EngineInstrumentationListenerAdapter {
    @Override
    public void onStart(Engine engine, boolean webSocket) {
        LaunchSpan launchSpan = LaunchSpan.current();
        if (launchSpan != null) launchSpan.setCommunicationProtocol(webSocket ? "WebSocket" : "JNLP");
    }

    @Override
    public void onConnected() {
        LaunchSpan launchSpan = LaunchSpan.current();
        if(launchSpan != null) launchSpan.end();
    }
}
