package io.jenkins.plugins.remotingopentelemetry.engine.metric;

import io.jenkins.plugins.remotingopentelemetry.engine.semconv.OpenTelemetryMetricsSemanticConventions;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricExporter;
import org.junit.Assert;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class OperatingSystemMXBeanMetricTest {
    @Test
    public void testProcessMetrics() throws Exception {
        Pattern filter = Pattern.compile("process\\..*");
        InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().build();
        IntervalMetricReader intervalMetricReader = IntervalMetricReader.builder()
                .setMetricExporter(inMemoryMetricExporter)
                .setMetricProducers(Collections.singleton(sdkMeterProvider))
                .setExportIntervalMillis(200)
                .build();

        new OperatingSystemMXBeanMetric(sdkMeterProvider, filter).register();

        intervalMetricReader.start();
        intervalMetricReader.forceFlush();
        intervalMetricReader.forceFlush();

        List<MetricData> metrics = inMemoryMetricExporter.getFinishedMetricItems();
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        if (operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            Assert.assertTrue(metrics.stream().anyMatch(metric ->
                    metric.getName().equals(OpenTelemetryMetricsSemanticConventions.PROCESS_CPU_TIME)));
            Assert.assertTrue(metrics.stream().anyMatch(metric ->
                    metric.getName().equals(OpenTelemetryMetricsSemanticConventions.PROCESS_CPU_LOAD)));
        }
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
    public void testSystemMetrics() throws Exception {
        Pattern filter = Pattern.compile("system\\..*");
        InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().build();
        IntervalMetricReader intervalMetricReader = IntervalMetricReader.builder()
                .setMetricExporter(inMemoryMetricExporter)
                .setMetricProducers(Collections.singleton(sdkMeterProvider))
                .setExportIntervalMillis(200)
                .build();

        new OperatingSystemMXBeanMetric(sdkMeterProvider, filter).register();

        intervalMetricReader.start();
        intervalMetricReader.forceFlush();
        intervalMetricReader.forceFlush();

        List<MetricData> metrics = inMemoryMetricExporter.getFinishedMetricItems();
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        Assert.assertTrue(metrics.stream().anyMatch(metric ->
                metric.getName().equals(OpenTelemetryMetricsSemanticConventions.SYSTEM_CPU_LOAD_AVERAGE_1M)));
        if (operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            Assert.assertTrue(metrics.stream().anyMatch(metric ->
                    metric.getName().equals(OpenTelemetryMetricsSemanticConventions.SYSTEM_CPU_LOAD)));
            Assert.assertTrue(metrics.stream().anyMatch(metric ->
                    metric.getName().equals(OpenTelemetryMetricsSemanticConventions.SYSTEM_MEMORY_USAGE)));
            Assert.assertTrue(metrics.stream().anyMatch(metric ->
                    metric.getName().equals(OpenTelemetryMetricsSemanticConventions.SYSTEM_MEMORY_UTILIZATION)));
            Assert.assertTrue(metrics.stream().anyMatch(metric ->
                    metric.getName().equals(OpenTelemetryMetricsSemanticConventions.SYSTEM_PAGING_USAGE)));
            Assert.assertTrue(metrics.stream().anyMatch(metric ->
                    metric.getName().equals(OpenTelemetryMetricsSemanticConventions.SYSTEM_PAGING_UTILIZATION)));
        }
        Assert.assertFalse(metrics.stream().anyMatch(metric ->
                metric.getName().equals(OpenTelemetryMetricsSemanticConventions.PROCESS_CPU_LOAD)));
        Assert.assertFalse(metrics.stream().anyMatch(metric ->
                metric.getName().equals(OpenTelemetryMetricsSemanticConventions.PROCESS_CPU_TIME)));

        inMemoryMetricExporter.reset();
        intervalMetricReader.shutdown();
    }

    @Test
    public void testFilesystemMetrics() throws Exception {
        Pattern filter = Pattern.compile("system\\.filesystem\\..*");
        InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().build();
        IntervalMetricReader intervalMetricReader = IntervalMetricReader.builder()
                .setMetricExporter(inMemoryMetricExporter)
                .setMetricProducers(Collections.singleton(sdkMeterProvider))
                .setExportIntervalMillis(60)
                .build();

        new FilesystemMetric(sdkMeterProvider, filter).register();

        intervalMetricReader.start();
        intervalMetricReader.forceFlush();
        intervalMetricReader.forceFlush();

        List<MetricData> metrics = inMemoryMetricExporter.getFinishedMetricItems();
        if (FilesystemMetric.MountInfo.createFromProcMounts() != null) {
            Assert.assertTrue(metrics.stream().anyMatch(metric ->
                    metric.getName().equals(OpenTelemetryMetricsSemanticConventions.SYSTEM_FILESYSTEM_USAGE)));
            Assert.assertTrue(metrics.stream().anyMatch(metric ->
                    metric.getName().equals(OpenTelemetryMetricsSemanticConventions.SYSTEM_FILESYSTEM_UTILIZATION)));
        }
    }
}
