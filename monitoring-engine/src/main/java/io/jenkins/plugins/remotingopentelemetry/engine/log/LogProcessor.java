package io.jenkins.plugins.remotingopentelemetry.engine.log;

import io.opentelemetry.sdk.common.CompletableResultCode;

public interface LogProcessor {
    void addLogRecord(TaggedLogRecord record);

    CompletableResultCode shutdown();

    CompletableResultCode forceFlush();
}
