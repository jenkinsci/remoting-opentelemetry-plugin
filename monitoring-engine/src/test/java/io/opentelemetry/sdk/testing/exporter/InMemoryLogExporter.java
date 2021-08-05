package io.opentelemetry.sdk.testing.exporter;

import io.jenkins.plugins.remotingopentelemetry.engine.log.LogExporter;
import io.jenkins.plugins.remotingopentelemetry.engine.log.TaggedLogRecord;
import io.opentelemetry.sdk.common.CompletableResultCode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class InMemoryLogExporter implements LogExporter {
    private final List<TaggedLogRecord> taggedLogRecords = new ArrayList<>();


    public static InMemoryLogExporter create() {
        return new InMemoryLogExporter();
    }

    public List<TaggedLogRecord> getTaggedLogRecords() {
        return Collections.unmodifiableList(new ArrayList<>(taggedLogRecords));
    }

    public void reset() {
        taggedLogRecords.clear();
    }

    @Override
    public CompletableResultCode export(Collection<TaggedLogRecord> logRecords) {
        taggedLogRecords.addAll(logRecords);
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        taggedLogRecords.clear();
        return CompletableResultCode.ofSuccess();
    }
}
