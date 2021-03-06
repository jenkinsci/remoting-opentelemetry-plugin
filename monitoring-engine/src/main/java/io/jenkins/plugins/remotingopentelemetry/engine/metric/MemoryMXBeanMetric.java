package io.jenkins.plugins.remotingopentelemetry.engine.metric;

import io.jenkins.plugins.remotingopentelemetry.engine.semconv.OpenTelemetryMetricsSemanticConventions;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.common.Labels;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.regex.Pattern;

public class MemoryMXBeanMetric {
    private final Meter meter;
    private final Pattern filterPattern;

    public MemoryMXBeanMetric(MeterProvider meterProvider, Pattern filterPattern) {
        meter = meterProvider.get(MemoryMXBean.class.getName());
        this.filterPattern = filterPattern;
    }

    public void register() {
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        if (filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.RUNTIME_JVM_MEMORY_AREA).matches()) {
            meter.longUpDownSumObserverBuilder(OpenTelemetryMetricsSemanticConventions.RUNTIME_JVM_MEMORY_AREA)
                    .setDescription("Bytes of a given JVM memory area.")
                    .setUnit("bytes")
                    .setUpdater(result -> {
                        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
                        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

                        result.observe(heapMemoryUsage.getUsed(), memoryLabels("used", "heap"));
                        result.observe(heapMemoryUsage.getCommitted(), memoryLabels("committed", "heap"));
                        if (heapMemoryUsage.getMax() >= 0) {
                            result.observe(heapMemoryUsage.getMax(), memoryLabels("max", "heap"));
                        }

                        result.observe(nonHeapMemoryUsage.getUsed(), memoryLabels("used", "non_heap"));
                        result.observe(nonHeapMemoryUsage.getCommitted(), memoryLabels("committed", "non_heap"));
                        if (nonHeapMemoryUsage.getMax() >= 0) {
                            result.observe(nonHeapMemoryUsage.getMax(), memoryLabels("max", "non_heap"));
                        }
                    })
                    .build();
        }
    }

    private Labels memoryLabels(String type, String area) {
        return Labels.of("type", type, "area", area);
    }
}
