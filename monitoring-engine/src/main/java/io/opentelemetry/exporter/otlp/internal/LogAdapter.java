package io.opentelemetry.exporter.otlp.internal;

import io.jenkins.plugins.remotingopentelemetry.engine.log.TaggedLogRecord;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.common.v1.KeyValueList;
import io.opentelemetry.proto.logs.v1.InstrumentationLibraryLogs;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LogAdapter {
    public static List<ResourceLogs> toProtoResourceLogs(Collection<TaggedLogRecord> logRecordList) {
        Map<Resource, Map<InstrumentationLibraryInfo, List<LogRecord>>> resourceAndLibraryMap =
                groupByResourceAndLibrary(logRecordList);
        List<ResourceLogs> resourceLogs = new ArrayList<>(resourceAndLibraryMap.size());
        resourceAndLibraryMap.forEach(
                (resource, libraryLogs) -> {
                    ResourceLogs.Builder resourceLogsBuilder =
                            ResourceLogs.newBuilder().setResource(ResourceAdapter.toProtoResource(resource));
                    libraryLogs.forEach(
                            (library, logs) -> {
                                resourceLogsBuilder.addInstrumentationLibraryLogs(buildInstrumentationLibraryLog(library, logs));
                            }
                    );
                    resourceLogs.add(resourceLogsBuilder.build());
                }
        );
        return resourceLogs;
    }

    private static InstrumentationLibraryLogs buildInstrumentationLibraryLog(
            InstrumentationLibraryInfo library, List<LogRecord> logs) {
        InstrumentationLibraryLogs.Builder logsBuilder =
                InstrumentationLibraryLogs.newBuilder()
                    .setInstrumentationLibrary(CommonAdapter.toProtoInstrumentationLibrary(library))
                    .addAllLogs(logs);
       if (library.getSchemaUrl() != null) {
           logsBuilder.setSchemaUrl(library.getSchemaUrl());
       }
       return logsBuilder.build();
    }

    private static Map<Resource, Map<InstrumentationLibraryInfo, List<LogRecord>>>
    groupByResourceAndLibrary(Collection<TaggedLogRecord> logRecordDataList) {
        Map<Resource, Map<InstrumentationLibraryInfo, List<LogRecord>>> result = new HashMap<>();
        for (TaggedLogRecord logRecordData : logRecordDataList) {
            Resource resource = logRecordData.getResource();
            Map<InstrumentationLibraryInfo, List<LogRecord>> libraryInfoListMap = result.computeIfAbsent(resource, unused -> new HashMap<>());
            InstrumentationLibraryInfo libraryInfo = logRecordData.getLibraryInfo();
            List<LogRecord> logRecordList = libraryInfoListMap.computeIfAbsent(libraryInfo, unused -> new ArrayList<>());
            logRecordList.add(toProtoLogRecord(logRecordData.getRecord()));
        }
        return result;
    }

    static LogRecord toProtoLogRecord(io.opentelemetry.sdk.logging.data.LogRecord logRecordData) {
        LogRecord.Builder builder = LogRecord.newBuilder();
        builder.setTimeUnixNano(logRecordData.getTimeUnixNano())
                .setSeverityNumberValue(logRecordData.getSeverity().getSeverityNumber())
                .setSeverityText(logRecordData.getSeverityText())
                .setBody(toCommonAnyValue(logRecordData.getBody()));

        logRecordData.getAttributes().forEach((key, value) -> {
            builder.addAttributes(KeyValue.newBuilder().setKey(key.toString()).setValue(AnyValue.newBuilder().setStringValue(value.toString())).build());
        });
        String severityText = logRecordData.getSeverityText();
        if (severityText != null) builder.setSeverityText(logRecordData.getSeverityText());
        String name = logRecordData.getName();
        if (name != null) builder.setName(name);
        return builder.build();
    }

    static AnyValue toCommonAnyValue(io.opentelemetry.sdk.logging.data.AnyValue anyValue) {
        AnyValue.Builder builder = AnyValue.newBuilder();
        switch (anyValue.getType()) {
            case BOOL:
                builder.setBoolValue(anyValue.getBoolValue());
                break;
            case ARRAY:
                ArrayValue.Builder arrayValueBuilder = ArrayValue.newBuilder();
                anyValue.getArrayValue().forEach((value) -> {
                    arrayValueBuilder.addValues(toCommonAnyValue(value));
                });
                builder.setArrayValue(arrayValueBuilder.build());
                break;
            case INT64:
                builder.setIntValue(anyValue.getLongValue());
                break;
            case DOUBLE:
                builder.setDoubleValue(anyValue.getDoubleValue());
                break;
            case KVLIST:
                KeyValueList.Builder kvListBuilder = KeyValueList.newBuilder();
                anyValue.getKvlistValue().forEach((key, value) -> {
                    kvListBuilder.addValues(KeyValue.newBuilder().setKey(key).setValue(toCommonAnyValue(value)).build());
                });
                builder.setKvlistValue(kvListBuilder.build());
                break;
            case STRING:
                builder.setStringValue(anyValue.getStringValue());
                break;
        }
        return builder.build();
    }
}
