package io.jenkins.plugins.remotingopentelemetry.engine.listener;

import hudson.remoting.EngineListener;
import io.jenkins.plugins.remotingopentelemetry.engine.span.ChannelKeepAliveSpan;

/**
 * Handles the events from {@link hudson.remoting.Engine}
 */
public class RemoteEngineListener implements EngineListener {
    @Override
    public void status(String s) {
        switch (s) {
            case "Connected":
                new ChannelKeepAliveSpan().start();
                break;
            case "Terminated":
                ChannelKeepAliveSpan span = ChannelKeepAliveSpan.current();
                if (span != null) {
                    span.end();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void status(String s, Throwable throwable) {

    }

    @Override
    public void error(Throwable throwable) {

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onReconnect() {

    }
}
