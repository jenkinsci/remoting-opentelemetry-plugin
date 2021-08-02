package io.jenkins.plugins.remotingopentelemetry.engine.log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.opentelemetry.exporter.otlp.internal.LogAdapter;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc.LogsServiceFutureStub;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class OtlpGrpcLogExporter implements LogExporter {

    private final ThrottlingLogger logger =
            new ThrottlingLogger(Logger.getLogger(OtlpGrpcLogExporter.class.getName()));

    private final LogsServiceGrpc.LogsServiceFutureStub logsService;
    private final ManagedChannel managedChannel;
    private final long timeoutNanos;

    OtlpGrpcLogExporter(ManagedChannel channel, long timeoutNanos) {
        this.managedChannel = channel;
        this.timeoutNanos = timeoutNanos;
        logsService = LogsServiceGrpc.newFutureStub(channel);
    }

    @Override
    public CompletableResultCode export(Collection<TaggedLogRecord> logs) {
        ExportLogsServiceRequest exportLogsServiceRequest =
                ExportLogsServiceRequest.newBuilder()
                        .addAllResourceLogs(LogAdapter.toProtoResourceLogs(logs))
                        .build();

        final CompletableResultCode result = new CompletableResultCode();
        final LogsServiceFutureStub exporter;
        if (timeoutNanos > 0) {
            exporter = logsService.withDeadlineAfter(timeoutNanos, TimeUnit.NANOSECONDS);
        } else {
            exporter = logsService;
        }

        Futures.addCallback(
                exporter.export(exportLogsServiceRequest),
                new FutureCallback<ExportLogsServiceResponse>() {
                    @Override
                    public void onSuccess(@Nullable ExportLogsServiceResponse exportLogsServiceResponse) {
                        result.succeed();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Status status = Status.fromThrowable(t);
                        switch (status.getCode()) {
                            case UNIMPLEMENTED:
                                logger.log(
                                        Level.SEVERE,
                                        "Failed to export logs. Server responded with UNIMPLEMENTED. "
                                                + "This usually means that your collector is not configured with an otlp "
                                                + "receiver in the \"pipelines\" section of the configuration. "
                                                + "Full error message: "
                                                + t.getMessage());
                                break;
                            case UNAVAILABLE:
                                logger.log(
                                        Level.SEVERE,
                                        "Failed to export logs. Server is UNAVAILABLE. "
                                                + "Make sure your collector is running and reachable from this network."
                                                + "Full error message: "
                                                + t.getMessage());
                                break;
                            default:
                                logger.log(
                                        Level.WARNING, "Failed to export logs. Error message: " + t.getMessage());
                                break;
                        }
                        logger.log(Level.FINEST, "Failed to export metrics. Details follow: " + t);
                        result.fail();
                    }
                },
                MoreExecutors.directExecutor());
        return result;
    }

    public static OtlpGrpcLogExporterBuilder builder() {
        return new OtlpGrpcLogExporterBuilder();
    }

    public static OtlpGrpcLogExporter getDefault() {
        return builder().build();
    }

    @Override
    public CompletableResultCode shutdown() {
        try {
            managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Failed to shutdown the gRPC channel", e);
            return CompletableResultCode.ofFailure();
        }
        return CompletableResultCode.ofSuccess();
    }
}
