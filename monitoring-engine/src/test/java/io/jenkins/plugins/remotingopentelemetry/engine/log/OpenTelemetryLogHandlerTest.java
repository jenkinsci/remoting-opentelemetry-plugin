package io.jenkins.plugins.remotingopentelemetry.engine.log;

import io.jenkins.plugins.remotingopentelemetry.engine.EngineConfiguration;
import io.jenkins.plugins.remotingopentelemetry.engine.OpenTelemetryProxy;
import io.jenkins.plugins.remotingopentelemetry.engine.RemotingResourceProvider;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogExporter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class OpenTelemetryLogHandlerTest {
    InMemoryLogExporter logExporter;

    @Before
    public void setup() throws Exception {
        EngineConfiguration config = new EngineConfiguration("http://localhost", "test");
        Resource resource = RemotingResourceProvider.create(config);
        logExporter = InMemoryLogExporter.create();
        LogProcessor logProcessor = new SimpleLogProcessor(logExporter);
        OpenTelemetryProxy.build(null, logProcessor, resource, config);
    }

    @Test
    public void testPublishLogRecord() throws Exception {
        OpenTelemetryLogHandler logHandler = new OpenTelemetryLogHandler();
        LogRecord record = new LogRecord(Level.WARNING, "application warning");
        logHandler.publish(record);
        record = new LogRecord(Level.INFO, "Hello world");
        logHandler.publish(record);

        List<TaggedLogRecord> records = logExporter.getTaggedLogRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("application warning", records.get(0).getRecord().getBody().getStringValue());
        Assert.assertEquals("Hello world", records.get(1).getRecord().getBody().getStringValue());
        Assert.assertEquals("jenkins remoting", records.get(1).getLibraryInfo().getName());
        Assert.assertEquals(
                "test",
                records.get(1).getResource().getAttributes().get(AttributeKey.stringKey("service.instance.id"))
        );
    }

    @After
    public void tearDown() throws Exception {
        logExporter.reset();
        OpenTelemetryProxy.clean();
    }

    static public class SimpleLogProcessor implements LogProcessor {
        private LogExporter logExporter;

        public SimpleLogProcessor(LogExporter logExporter) {
            this.logExporter = logExporter;
        }

        @Override
        public void addLogRecord(TaggedLogRecord record) {
            logExporter.export(Collections.singleton(record));
        }

        @Override
        public CompletableResultCode shutdown() {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode forceFlush() {
            return CompletableResultCode.ofSuccess();
        }
    }
}
