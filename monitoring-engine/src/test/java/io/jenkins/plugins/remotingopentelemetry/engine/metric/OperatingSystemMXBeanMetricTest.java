package io.jenkins.plugins.remotingopentelemetry.engine.metric;

import io.jenkins.plugins.remotingopentelemetry.engine.semconv.OpenTelemetryMetricsSemanticConventions;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricExporter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class OperatingSystemMXBeanMetricTest {
    @Test
    public void testUseSystemMetricArgument() throws Exception {
        InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().build();
        IntervalMetricReader intervalMetricReader = IntervalMetricReader.builder()
                .setMetricExporter(inMemoryMetricExporter)
                .setMetricProducers(Collections.singleton(sdkMeterProvider))
                .setExportIntervalMillis(200)
                .build();

        new OperatingSystemMXBeanMetric(sdkMeterProvider, false, true).register();

        intervalMetricReader.start();

        Thread.sleep(600);

        List<MetricData> metrics = inMemoryMetricExporter.getFinishedMetricItems();
        Assert.assertFalse(metrics.stream().anyMatch(metric ->
                metric.getName().equals(OpenTelemetryMetricsSemanticConventions.SYSTEM_CPU_LOAD)));
        Assert.assertFalse(metrics.stream().anyMatch(metric ->
                metric.getName().equals(OpenTelemetryMetricsSemanticConventions.SYSTEM_CPU_LOAD_AVERAGE_1M)));
        Assert.assertFalse(metrics.stream().anyMatch(metric ->
                metric.getName().equals(OpenTelemetryMetricsSemanticConventions.SYSTEM_MEMORY_USAGE)));
        Assert.assertFalse(metrics.stream().anyMatch(metric ->
                metric.getName().equals(OpenTelemetryMetricsSemanticConventions.SYSTEM_MEMORY_UTILIZATION)));
        Assert.assertFalse(metrics.stream().anyMatch(metric ->
                metric.getName().equals(OpenTelemetryMetricsSemanticConventions.SYSTEM_PAGING_USAGE)));
        Assert.assertFalse(metrics.stream().anyMatch(metric ->
                metric.getName().equals(OpenTelemetryMetricsSemanticConventions.SYSTEM_PAGING_UTILIZATION)));

        inMemoryMetricExporter.reset();
        intervalMetricReader.shutdown();
    }

    @Test
    public void testUseProcessMetricArgument() throws Exception {
        InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().build();
        IntervalMetricReader intervalMetricReader = IntervalMetricReader.builder()
                .setMetricExporter(inMemoryMetricExporter)
                .setMetricProducers(Collections.singleton(sdkMeterProvider))
                .setExportIntervalMillis(200)
                .build();

        new OperatingSystemMXBeanMetric(sdkMeterProvider, true, false).register();

        intervalMetricReader.start();

        Thread.sleep(600);

        List<MetricData> metrics = inMemoryMetricExporter.getFinishedMetricItems();
        Assert.assertFalse(metrics.stream().anyMatch(metric ->
                metric.getName().equals(OpenTelemetryMetricsSemanticConventions.PROCESS_CPU_LOAD)));
        Assert.assertFalse(metrics.stream().anyMatch(metric ->
                metric.getName().equals(OpenTelemetryMetricsSemanticConventions.PROCESS_CPU_TIME)));

        inMemoryMetricExporter.reset();
        intervalMetricReader.shutdown();
    }
}
