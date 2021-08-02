package io.jenkins.plugins.remotingopentelemetry.engine.log;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logging.data.LogRecord;
import io.opentelemetry.sdk.resources.Resource;

public class TaggedLogRecord {
    private final LogRecord record;
    private final Resource resource;
    private final InstrumentationLibraryInfo libraryInfo;

    TaggedLogRecord(LogRecord record, Resource resource, InstrumentationLibraryInfo libraryInfo) {
        this.record = record;
        this.resource = resource;
        this.libraryInfo = libraryInfo;
    }

    public LogRecord getRecord() {
        return record;
    }

    public Resource getResource() {
        return resource;
    }

    public InstrumentationLibraryInfo getLibraryInfo() {
        return libraryInfo;
    }
}
