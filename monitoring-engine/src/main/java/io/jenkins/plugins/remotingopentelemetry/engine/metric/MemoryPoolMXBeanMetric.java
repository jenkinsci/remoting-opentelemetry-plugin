package io.jenkins.plugins.remotingopentelemetry.engine.metric;

import io.jenkins.plugins.remotingopentelemetry.engine.semconv.OpenTelemetryMetricsSemanticConventions;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.common.Labels;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MemoryPoolMXBeanMetric {
    private final Meter meter;
    private final Pattern filterPattern;
    public MemoryPoolMXBeanMetric(MeterProvider meterProvider, Pattern filterPattern) {
        meter = meterProvider.get(MemoryPoolMXBean.class.getName());
        this.filterPattern = filterPattern;
    }

    public void register() {
        List<MemoryPoolMXBean> poolBeans = ManagementFactory.getMemoryPoolMXBeans();

        List<Labels> usedLabelSets = new ArrayList<>(poolBeans.size());
        List<Labels> committedLabelSets = new ArrayList<>(poolBeans.size());
        List<Labels> maxLabelSets = new ArrayList<>(poolBeans.size());
        for (MemoryPoolMXBean pool : poolBeans) {
            usedLabelSets.add(Labels.of("type", "used", "pool", pool.getName()));
            committedLabelSets.add(Labels.of("type", "committed", "pool", pool.getName()));
            maxLabelSets.add(Labels.of("type", "max", "pool", pool.getName()));
        }

        if (filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.RUNTIME_JVM_MEMORY_POOL).matches()) {
            meter.longUpDownSumObserverBuilder(OpenTelemetryMetricsSemanticConventions.RUNTIME_JVM_MEMORY_POOL)
                    .setDescription("Bytes of a given JVM memory pool.")
                    .setUnit("bytes")
                    .setUpdater(result -> {
                        for (int i = 0; i< poolBeans.size(); i++) {
                            MemoryUsage poolUsage = poolBeans.get(i).getUsage();
                            if (poolUsage != null) {
                                result.observe(poolUsage.getUsed(), usedLabelSets.get(i));
                                result.observe(poolUsage.getCommitted(), committedLabelSets.get(i));
                                if (poolUsage.getMax() >= 0) {
                                    result.observe(poolUsage.getMax(), maxLabelSets.get(i));
                                }
                            }
                        }
                    })
                    .build();
        }
    }
}
