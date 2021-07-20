package io.jenkins.plugins.remotingopentelemetry.engine.span;

import javax.annotation.Nullable;

public class LaunchSpan extends MonitoringSpan {
    static final String SPAN_NAME = "Launch";

    @Override
    protected String getSpanName() { return SPAN_NAME; }

    @Nullable
    private static LaunchSpan currentSpan = null;

    @Nullable
    public static synchronized  LaunchSpan current() {
        return currentSpan;
    }

    private static synchronized void setCurrent(LaunchSpan span) {
        currentSpan = span;
    }

    public LaunchSpan start() {
        LaunchSpan currentSpan = current();
        if (currentSpan != null) currentSpan.end();

        super.start();

        setCurrent(this);
        return this;
    }

    public LaunchSpan setCommunicationProtocol(String protocolName) {
        setAttribute("jenkins.remoting.communication.protocol", protocolName);
        return this;
    }

    public LaunchSpan setRemotingVersion(String version) {
        setAttribute("jenkins.remoting.version", version);
        return this;
    }

    public void end() {
        super.end();
        setCurrent(null);
    }
}
