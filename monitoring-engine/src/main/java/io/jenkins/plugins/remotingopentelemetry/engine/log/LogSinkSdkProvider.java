/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.remotingopentelemetry.engine.log;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.logging.LogSink;
import io.opentelemetry.sdk.logging.data.LogRecord;
import io.opentelemetry.sdk.resources.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class LogSinkSdkProvider {
    private final List<LogProcessor> processors = new ArrayList<>();
    private final Resource resource;
    private final ComponentRegistry<SdkLogSink> registry;

    public static LogSinkSdkProviderBuilder builder() {
        return new LogSinkSdkProviderBuilder();
    }

    LogSinkSdkProvider(Resource resource) {
        this.resource = resource;
        this.registry = new ComponentRegistry<>(SdkLogSink::new);
    }

    public LogSink get(String instrumentationName) {
        return get(instrumentationName, null);
    }

    public LogSink get(String instrumentationName, String instrumentationVersion) {
        return registry.get(instrumentationName, instrumentationVersion);
    }

    public void addLogProcessor(LogProcessor processor) {
        processors.add(Objects.requireNonNull(processor, "Processor can not be null"));
    }

    public CompletableResultCode forceFlush() {
        final List<CompletableResultCode> processorResults = new ArrayList<>(processors.size());
        for (LogProcessor processor : processors) {
            processorResults.add(processor.forceFlush());
        }
        return CompletableResultCode.ofAll(processorResults);
    }

    public CompletableResultCode shutdown() {
        Collection<CompletableResultCode> processorResults = new ArrayList<>(processors.size());
        for (LogProcessor processor : processors) {
            processorResults.add(processor.shutdown());
        }
        return CompletableResultCode.ofAll(processorResults);
    }

    private class SdkLogSink implements LogSink {
        private final InstrumentationLibraryInfo libraryInfo;

        SdkLogSink(InstrumentationLibraryInfo libraryInfo) {
            this.libraryInfo = libraryInfo;
        }

        @Override
        public void offer(@NonNull LogRecord record) {
            for (LogProcessor processor : processors) {
                processor.addLogRecord(new TaggedLogRecord(record, resource, libraryInfo));
            }
        }
    }
}
