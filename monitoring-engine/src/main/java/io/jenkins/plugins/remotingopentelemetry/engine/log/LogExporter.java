package io.jenkins.plugins.remotingopentelemetry.engine.log;

import io.opentelemetry.sdk.common.CompletableResultCode;

import java.util.Collection;

public interface LogExporter {
    CompletableResultCode export(Collection<TaggedLogRecord> logs);

    CompletableResultCode shutdown();
}
