package io.jenkins.plugins.remotingopentelemetry;

import com.google.common.util.concurrent.AbstractCheckedFuture;
import com.google.protobuf.GeneratedMessageV3;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.slaves.ComputerListener;
import io.grpc.Deadline;
import io.grpc.MethodDescriptor;
import io.grpc.internal.ManagedChannelImplBuilder;
import io.grpc.netty.shaded.io.netty.util.AbstractConstant;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.protobuf.lite.ProtoLiteUtils;
import io.grpc.stub.ClientCalls;
import io.jenkins.plugins.remotingopentelemetry.commands.SyncMonitoringEngineCommand;
import io.jenkins.plugins.remotingopentelemetry.engine.EngineConfiguration;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.internal.SpanAdapter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.perfmark.PerfMark;
import jenkins.model.Jenkins.MasterComputer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transfers the monitoring engine and the configurations to the online agents
 *
 * @author Akihiro Kiuchi
 */
@Extension
public final class RemotingOpenTelemetryComputerListener extends ComputerListener {
    final static Logger LOGGER = Logger.getLogger(RemotingOpenTelemetryComputerListener.class.getName());

    @Override
    public final void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
        if (c instanceof MasterComputer) return;

        VirtualChannel vc = c.getChannel();
        if (vc == null) return;

        EngineConfiguration config = RemotingOpenTelemetryConfiguration.get().export();
        try {
            vc.call(new SyncMonitoringEngineCommand(config));
        } catch (IOException | InterruptedException e) {
            String command = SyncMonitoringEngineCommand.class.getName();
            Node node = c.getNode();
            String nodeName = node == null ? "unknown" : node.getNodeName();
            LOGGER.log(Level.WARNING, "Fail to call " + command + " for " + nodeName , e);
        }

        if (vc instanceof Channel) {
            Channel ch = (Channel) vc;

            // Send JAR files to the agent in advance to run the MonitoringEngine even in offline.
            // TODO: Enable to auto-detect the dependent JAR files.
            ch.preloadJar(getClass().getClassLoader(), OpenTelemetrySdk.class);          // opentelemetry-sdk
            ch.preloadJar(getClass().getClassLoader(), CompletableResultCode.class);     // opentelemetry-sdk-commmon
            ch.preloadJar(getClass().getClassLoader(), SdkTracerProvider.class);         // opentelemetry-sdk-trace
            ch.preloadJar(getClass().getClassLoader(), OpenTelemetry.class);             // opentelemetry-api
            ch.preloadJar(getClass().getClassLoader(), LoggingSpanExporter.class);       // opentelemetry-exporter-logging
            ch.preloadJar(getClass().getClassLoader(), Scope.class);                     // opentelemetry-context
            ch.preloadJar(getClass().getClassLoader(), OtlpGrpcSpanExporter.class);      // opentelemetry-exporter-otlp-trace
            ch.preloadJar(getClass().getClassLoader(), SpanAdapter.class);               // opentelemetry-exporter-otlp-common
            ch.preloadJar(getClass().getClassLoader(), ExportTraceServiceRequest.class); // opentelemetry-proto
            ch.preloadJar(getClass().getClassLoader(), GeneratedMessageV3.class);        // com.google.protobuf:protobuf-java
            ch.preloadJar(getClass().getClassLoader(), Deadline.class);                  // io.grpc:grpc-context
            ch.preloadJar(getClass().getClassLoader(), MethodDescriptor.class);          // io.grpc:grpc-api
            ch.preloadJar(getClass().getClassLoader(), ProtoUtils.class);                // io.grpc:grpc-protobuf
            ch.preloadJar(getClass().getClassLoader(), ProtoLiteUtils.class);            // io.grpc:grpc-protobuf-lite
            ch.preloadJar(getClass().getClassLoader(), ManagedChannelImplBuilder.class); // io.grpc:grpc-core
            ch.preloadJar(getClass().getClassLoader(), AbstractConstant.class);          // io.grpc:grpc-netty-shaded
            ch.preloadJar(getClass().getClassLoader(), ClientCalls.class);               // io.grpc:grpc-stub
            ch.preloadJar(getClass().getClassLoader(), AbstractCheckedFuture.class);     // com.google.guava:guava
            ch.preloadJar(getClass().getClassLoader(), PerfMark.class);                  // io.perfmark:perfmark-api
        }
    }
}
