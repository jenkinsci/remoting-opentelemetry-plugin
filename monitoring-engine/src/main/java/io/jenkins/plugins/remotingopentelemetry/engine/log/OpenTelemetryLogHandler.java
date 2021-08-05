package io.jenkins.plugins.remotingopentelemetry.engine.log;

import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.logging.LogSink;
import io.opentelemetry.sdk.logging.data.LogRecord.Severity;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public final class OpenTelemetryLogHandler extends Handler {
    private Formatter formatter = new Formatter() {
        @Override
        public String format(LogRecord record) {
            return null;
        }
    };

    public OpenTelemetryLogHandler() {
        super();
        setFilter(new NettyClientHandlerLoggerFilter());
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) return;
        LogSink logSink = OpenTelemetryProxy.getLogSink();
        if (logSink == null) return;

        logSink.offer(toOtelLogRecord(record));
    }

    @Override
    public void flush() {
        // TODO: flush logs to OTLP endpoint
    }

    @Override
    public void close() throws SecurityException {}

    private io.opentelemetry.sdk.logging.data.LogRecord toOtelLogRecord(LogRecord logRecord) {
        io.opentelemetry.sdk.logging.data.LogRecordBuilder builder = io.opentelemetry.sdk.logging.data.LogRecord.builder();
        AttributesBuilder attributesBuilder = Attributes.builder()
                .put("log.level", logRecord.getLevel().getName());

        if (logRecord.getSourceClassName() != null) attributesBuilder.put("code.namespace",  logRecord.getSourceClassName());
        if (logRecord.getSourceMethodName() != null) attributesBuilder.put("code.function",  logRecord.getSourceMethodName());

        Throwable throwable = logRecord.getThrown();
        if (throwable != null) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            throwable.printStackTrace(new PrintStream(baos));
            attributesBuilder.put("exception.type", throwable.getClass().getName())
                    .put("exception.message", throwable.getLocalizedMessage())
                    .put("exception.stacktrace", baos.toString());
        }

        builder.setName(logRecord.getMessage())
                .setSeverity(toSeverity(logRecord.getLevel()))
                .setSeverityText(logRecord.getLevel().getName())
                .setUnixTimeMillis(logRecord.getMillis())
                .setAttributes(attributesBuilder.build())
                .setBody(formatter.formatMessage(logRecord));

        return builder.build();
    }

    private io.opentelemetry.sdk.logging.data.LogRecord.Severity toSeverity(Level level) {
        int levelInt = level.intValue();
        Severity severity = Severity.UNDEFINED_SEVERITY_NUMBER;
        if (levelInt <= Level.FINEST.intValue()) {
            severity = Severity.TRACE;
        } else if (levelInt <= Level.FINER.intValue()) {
            severity = Severity.DEBUG;
        } else if (levelInt <= Level.FINE.intValue()) {
            severity = Severity.DEBUG2;
        } else if (levelInt <= Level.CONFIG.intValue()) {
            severity = Severity.DEBUG3;
        } else if (levelInt <= Level.INFO.intValue()) {
            severity = Severity.INFO;
        } else if (levelInt <= Level.WARNING.intValue()) {
            severity = Severity.WARN;
        } else if (levelInt <= Level.SEVERE.intValue()) {
            severity = Severity.ERROR;
        }
        return severity;
    }

    private static final class NettyClientHandlerLoggerFilter implements Filter {
        private final String FILTERING_LOGGER_NAME = "io.grpc.netty.shaded.io.grpc.netty.NettyClientHandler";

        @Override
        public boolean isLoggable(LogRecord record) {
            String loggerName = record.getLoggerName();
            return loggerName == null || !loggerName.equals(FILTERING_LOGGER_NAME);
        }
    }
}
