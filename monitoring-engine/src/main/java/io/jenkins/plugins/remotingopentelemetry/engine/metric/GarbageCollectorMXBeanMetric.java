package io.jenkins.plugins.remotingopentelemetry.engine.metric;

import io.jenkins.plugins.remotingopentelemetry.engine.semconv.OpenTelemetryMetricsSemanticConventions;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.common.Labels;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GarbageCollectorMXBeanMetric {
    private final Meter meter;
    private final Pattern filterPattern;
    public GarbageCollectorMXBeanMetric(MeterProvider meterProvider, Pattern filterPattern) {
        meter = meterProvider.get(GarbageCollectorMXBean.class.getName());
        this.filterPattern = filterPattern;
    }

    public void register() {
        List<GarbageCollectorMXBean> garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans();

        List<Labels> labelSets = new ArrayList<>(garbageCollectors.size());
        for (final GarbageCollectorMXBean gc : garbageCollectors) {
            labelSets.add(Labels.of("gc", gc.getName()));
        }

        if(filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.RUNTIME_JVM_GC_TIME).matches()) {
            meter.longSumObserverBuilder(OpenTelemetryMetricsSemanticConventions.RUNTIME_JVM_GC_TIME)
                    .setDescription("Time spent in a given JVM garbage collector in milliseconds.")
                    .setUnit("ms")
                    .setUpdater(
                            result -> {
                                for (int i = 0; i < garbageCollectors.size(); i++) {
                                    result.observe(garbageCollectors.get(i).getCollectionTime(), labelSets.get(i));
                                }
                            }
                    )
                    .build();
        }

        if (filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.RUNTIME_JVM_GC_COUNT).matches()) {
            meter.longSumObserverBuilder(OpenTelemetryMetricsSemanticConventions.RUNTIME_JVM_GC_COUNT)
                    .setDescription("The number of collections that have occurred for a given JVM garbage collector.")
                    .setUnit("collections")
                    .setUpdater(result -> {
                        for (int i = 0; i < garbageCollectors.size(); i++) {
                            result.observe(garbageCollectors.get(i).getCollectionCount(), labelSets.get(i));
                        }
                    })
                    .build();
        }
    }
}
