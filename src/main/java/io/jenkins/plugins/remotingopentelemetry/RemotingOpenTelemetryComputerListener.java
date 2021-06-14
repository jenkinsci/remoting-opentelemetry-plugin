package io.jenkins.plugins.remotingopentelemetry;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.slaves.ComputerListener;
import io.jenkins.plugins.remotingopentelemetry.commands.SyncMonitoringEngineCommand;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import jenkins.model.Jenkins.MasterComputer;

import java.io.IOException;

/**
 * Transfers the monitoring engine and the configurations to the online agents
 *
 * @author Akihiro Kiuchi
 */
@Extension
public final class RemotingOpenTelemetryComputerListener extends ComputerListener {
    @Override
    public final void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
        if (c instanceof MasterComputer) return;
        VirtualChannel vc = c.getChannel();
        vc.call(new SyncMonitoringEngineCommand());
        if (vc instanceof Channel) {
            Channel ch = (Channel) vc;

            // Send JAR files to the agent in advance to run the MonitoringEngine even in offline.
            // TODO: Enable to auto-detect the dependent JAR files.
            ch.preloadJar(getClass().getClassLoader(), OpenTelemetrySdk.class);      // opentelemetry-sdk-<version>.jar
            ch.preloadJar(getClass().getClassLoader(), CompletableResultCode.class); // opentelemetry-sdk-commmon-<version>.jar
            ch.preloadJar(getClass().getClassLoader(), SdkTracerProvider.class);     // opentelemetry-sdk-trace-<version>.jar
            ch.preloadJar(getClass().getClassLoader(), OpenTelemetry.class);         // opentelemetry-api-<version>.jar
            ch.preloadJar(getClass().getClassLoader(), LoggingSpanExporter.class);   // opentelemetry-exporter-logging-<version>.jar
            ch.preloadJar(getClass().getClassLoader(), Scope.class);                 // opentelemetry-context-<version>.jar
        }
    }
}
