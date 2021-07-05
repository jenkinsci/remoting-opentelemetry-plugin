package io.jenkins.plugins.remotingopentelemetry.engine.listener;

import hudson.remoting.EngineListener;
import io.jenkins.plugins.remotingopentelemetry.engine.span.ChannelInitializationSpan;
import io.jenkins.plugins.remotingopentelemetry.engine.span.JnlpInitializationSpan;
import io.jenkins.plugins.remotingopentelemetry.engine.span.JnlpEndpointResolvingSpan;

/**
 * Handles the events from {@link hudson.remoting.Engine}
 */
public class RemoteEngineListener implements EngineListener {
    private boolean connecting = false;

    @Override
    public void status(String s) {
       status(s, null);
    }

    @Override
    public void status(String s, Throwable throwable) {
        if (s.startsWith("Locating server among")) {
            connecting = true;
            ChannelInitializationSpan channelInitializationSpan = ChannelInitializationSpan.current();
            if (channelInitializationSpan == null) {
                channelInitializationSpan = new ChannelInitializationSpan();
                channelInitializationSpan.start();
            }
            String candidates = s.replaceAll("^Locating server among", "");
            JnlpEndpointResolvingSpan jnlpEndpointResolvingSpan = new JnlpEndpointResolvingSpan();
            jnlpEndpointResolvingSpan.start(channelInitializationSpan);
            jnlpEndpointResolvingSpan.setEndpointCandidate(candidates);

        } else if (s.startsWith("Could not resolve JNLP agent endpoint")) {
            JnlpEndpointResolvingSpan jnlpEndpointResolvingSpan = JnlpEndpointResolvingSpan.current();
            if (jnlpEndpointResolvingSpan != null) {
                jnlpEndpointResolvingSpan.recordException(throwable).end();
            }
            ChannelInitializationSpan channelInitializationSpan = ChannelInitializationSpan.current();
            if (channelInitializationSpan != null) {
                channelInitializationSpan.recordException(throwable).end();
            }
        } else if (s.startsWith("Could not resolve server among ")) {
            JnlpEndpointResolvingSpan jnlpEndpointResolvingSpan = JnlpEndpointResolvingSpan.current();
            if (jnlpEndpointResolvingSpan != null) {
                jnlpEndpointResolvingSpan.setMessage(s).end();
            }
            ChannelInitializationSpan channelInitializationSpan = ChannelInitializationSpan.current();
            if (channelInitializationSpan != null) {
                channelInitializationSpan.setMessage(s).end();
            }
        } else if (s.startsWith("Agent discovery successful")) {
            JnlpEndpointResolvingSpan jnlpEndpointResolvingSpan = JnlpEndpointResolvingSpan.current();
            if (jnlpEndpointResolvingSpan != null) {
                jnlpEndpointResolvingSpan.setMessage(s).end();
            }
        } else if (s.equals("Handshaking")) {
            ChannelInitializationSpan channelInitializationSpan = ChannelInitializationSpan.current();

            JnlpInitializationSpan jnlpInitializationSpan =  new JnlpInitializationSpan();
            jnlpInitializationSpan.start(channelInitializationSpan);

        } else if (s.matches("^Protocol .* is not enabled, skipping$")) {
            ChannelInitializationSpan channelInitializationSpan = ChannelInitializationSpan.current();
            String protocolName = s.replaceAll("^Protocol | is not enabled, skipping$", "");

            JnlpInitializationSpan jnlpInitializationSpan = JnlpInitializationSpan.current();
            if (jnlpInitializationSpan != null) {
                jnlpInitializationSpan
                        .setProtocolName(protocolName)
                        .setMessage(s)
                        .end();
            }
            new JnlpInitializationSpan().start(channelInitializationSpan);
        } else if (s.matches("^Server reports protocol .* not supported, skipping$")) {
            ChannelInitializationSpan channelInitializationSpan = ChannelInitializationSpan.current();
            String protocolName = s.replaceAll("^Server reports protocol | not supported, skipping$", "");

            JnlpInitializationSpan jnlpInitializationSpan = JnlpInitializationSpan.current();
            if (jnlpInitializationSpan != null) {
                jnlpInitializationSpan
                        .setProtocolName(protocolName)
                        .setMessage(s)
                        .end();
            }
            new JnlpInitializationSpan().start(channelInitializationSpan);
        } else if (s.startsWith("Trying protocol: ")) {
            String protocolName = s.replaceFirst("^Trying protocol: ", "");
            JnlpInitializationSpan jnlpInitializationSpan = JnlpInitializationSpan.current();
            if (jnlpInitializationSpan != null) {
                jnlpInitializationSpan.setProtocolName(protocolName);
            }
        } else if (s.startsWith("Protocol ") && throwable != null) {
            ChannelInitializationSpan channelInitializationSpan = ChannelInitializationSpan.current();
            JnlpInitializationSpan jnlpInitializationSpan = JnlpInitializationSpan.current();
            if (jnlpInitializationSpan != null) {
                jnlpInitializationSpan
                        .setMessage(s)
                        .recordException(throwable)
                        .end();
            }
            new JnlpInitializationSpan().start(channelInitializationSpan);
        } else if (s.equals("Connected")) {
            JnlpInitializationSpan jnlpInitializationSpan = JnlpInitializationSpan.current();
            if (jnlpInitializationSpan != null) jnlpInitializationSpan.end();

            ChannelInitializationSpan channelInitializationSpan = ChannelInitializationSpan.current();
            if (channelInitializationSpan != null) channelInitializationSpan.end();
        }
    }

    @Override
    public void error(Throwable throwable) {
        if (!connecting) return;

        JnlpEndpointResolvingSpan jnlpEndpointResolvingSpan = JnlpEndpointResolvingSpan.current();
        if (jnlpEndpointResolvingSpan != null) {
            jnlpEndpointResolvingSpan.recordException(throwable).end();
        }

        JnlpInitializationSpan jnlpInitializationSpan = JnlpInitializationSpan.current();
        if (jnlpInitializationSpan != null) {
            if (jnlpInitializationSpan.getProtocolName() == null) {
                jnlpInitializationSpan.abort();
            } else {
                jnlpInitializationSpan.end();
            }
        }

        ChannelInitializationSpan channelInitializationSpan = ChannelInitializationSpan.current();
        if (channelInitializationSpan != null) {
            channelInitializationSpan.recordException(throwable).end();
        }
    }

    @Override
    public void onDisconnect() {}

    @Override
    public void onReconnect() {}
}
