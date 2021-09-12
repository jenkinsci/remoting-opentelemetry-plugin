package io.jenkins.plugins.remotingopentelemetry.engine.metric;

import io.jenkins.plugins.remotingopentelemetry.engine.semconv.OpenTelemetryMetricsSemanticConventions;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricExporter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ConnectionEstablishmentsCountMetricTest {
    @Test
    public void testConnectionEstablishmentsCounter() throws Exception {
        Pattern filter = Pattern.compile("jenkins\\.agent\\.connection\\.establishments\\..*");
        InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().build();
        IntervalMetricReader intervalMetricReader = IntervalMetricReader.builder()
                .setMetricExporter(inMemoryMetricExporter)
                .setMetricProducers(Collections.singleton(sdkMeterProvider))
                .setExportIntervalMillis(200)
                .build();

        new ConnectionEstablishmentsCountMetric(sdkMeterProvider, filter).register();

        intervalMetricReader.start();
        LongCounter counter = ConnectionEstablishmentsCountMetric.getCounter();
        counter.add(1);
        counter.add(1);
        counter.add(1);
        intervalMetricReader.forceFlush();
        intervalMetricReader.forceFlush();

        List<MetricData> metrics = inMemoryMetricExporter.getFinishedMetricItems();
        Assert.assertTrue(metrics.stream().anyMatch(metric ->
                metric.getName().equals(OpenTelemetryMetricsSemanticConventions.REMOTING_CONNECTION_ESTABLISHMENTS_COUNT)));

        Iterator<MetricData> itr = metrics.iterator();
        for(MetricData metricData = itr.next(); itr.hasNext(); metricData = itr.next()) {
            if (metricData.getName().equals(OpenTelemetryMetricsSemanticConventions.REMOTING_CONNECTION_ESTABLISHMENTS_COUNT)) {
                Assert.assertEquals(3, metricData.getLongSumData().getPoints().stream().mapToLong(point -> point.getValue()).sum());
            }
        }
    }
}
