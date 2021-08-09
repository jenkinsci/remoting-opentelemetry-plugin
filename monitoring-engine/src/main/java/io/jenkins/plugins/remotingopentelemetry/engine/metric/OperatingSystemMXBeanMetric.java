package io.jenkins.plugins.remotingopentelemetry.engine.metric;

import io.jenkins.plugins.remotingopentelemetry.engine.semconv.OpenTelemetryMetricsSemanticConventions;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.regex.Pattern;

public class OperatingSystemMXBeanMetric {
    private final Meter meter;
    private final Pattern filterPattern;

    public OperatingSystemMXBeanMetric(
            SdkMeterProvider sdkMeterProvider,
            Pattern filterPattern) {
        meter = sdkMeterProvider.get(OperatingSystemMXBean.class.getName());
        this.filterPattern = filterPattern;
    }

    public void register() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

        if (operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean) operatingSystemMXBean;
            registerSystemMetrics(osBean);
            registerProcessMetrics(osBean);
        } else {
            registerSystemMetrics(operatingSystemMXBean);
        }
    }

    private void registerSystemMetrics(OperatingSystemMXBean operatingSystemMXBean) {
        if (filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.SYSTEM_CPU_LOAD_AVERAGE_1M).matches()) {
            meter.doubleValueObserverBuilder(OpenTelemetryMetricsSemanticConventions.SYSTEM_CPU_LOAD_AVERAGE_1M)
                    .setDescription("System CPU load average 1 minutes")
                    .setUpdater(result -> result.observe(operatingSystemMXBean.getSystemLoadAverage(), Labels.empty()))
                    .build();
        }
    }

    private void registerSystemMetrics(com.sun.management.OperatingSystemMXBean osBean) {
        if (filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.SYSTEM_CPU_LOAD_AVERAGE_1M).matches()) {
            meter.doubleValueObserverBuilder(OpenTelemetryMetricsSemanticConventions.SYSTEM_CPU_LOAD_AVERAGE_1M)
                    .setDescription("System CPU load average 1 minutes")
                    .setUpdater(result -> result.observe(osBean.getSystemLoadAverage(), Labels.empty()))
                    .build();
        }

        if (filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.SYSTEM_CPU_LOAD).matches()) {
            meter.doubleValueObserverBuilder(OpenTelemetryMetricsSemanticConventions.SYSTEM_CPU_LOAD)
                    .setDescription("System CPU load (0.0 to 1.0)")
                    .setUnit("1")
                    .setUpdater(result -> result.observe(osBean.getSystemCpuLoad(), Labels.empty()))
                    .build();
        }

        if (filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.SYSTEM_MEMORY_USAGE).matches()) {
            meter.longUpDownSumObserverBuilder(OpenTelemetryMetricsSemanticConventions.SYSTEM_MEMORY_USAGE)
                    .setDescription("System memory usage")
                    .setUnit("bytes")
                    .setUpdater(result -> {
                        final long totalSize = osBean.getTotalPhysicalMemorySize();
                        final long freeSize = osBean.getFreePhysicalMemorySize();
                        final long usedSize = totalSize - freeSize;
                        result.observe(freeSize, OpenTelemetryMetricsSemanticConventions.STATE_FREE);
                        result.observe(usedSize, OpenTelemetryMetricsSemanticConventions.STATE_USED);
                    })
                    .build();
        }

        if (filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.SYSTEM_MEMORY_UTILIZATION).matches()) {
            meter.doubleValueObserverBuilder(OpenTelemetryMetricsSemanticConventions.SYSTEM_MEMORY_UTILIZATION)
                    .setDescription("System memory utilization (0.0 to 1.0)")
                    .setUnit("1")
                    .setUpdater(result -> {
                        final long totalSize = osBean.getTotalPhysicalMemorySize();
                        final long freeSize = osBean.getFreePhysicalMemorySize();
                        final long usedSize = totalSize - freeSize;
                        try {
                            final BigDecimal utilization = new BigDecimal(usedSize)
                                    .divide(new BigDecimal(totalSize), MathContext.DECIMAL64);
                            result.observe(utilization.doubleValue(), Labels.empty());
                        } catch (ArithmeticException e) {
                            result.observe(0, Labels.empty());
                        }
                    })
                    .build();
        }

        if (filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.SYSTEM_PAGING_USAGE).matches()) {
            meter.longUpDownSumObserverBuilder(OpenTelemetryMetricsSemanticConventions.SYSTEM_PAGING_USAGE)
                    .setDescription("system swap usage")
                    .setUnit("bytes")
                    .setUpdater(result -> {
                        final long freeSize = osBean.getFreeSwapSpaceSize();
                        final long totalSize = osBean.getTotalSwapSpaceSize();
                        final long usedSize = totalSize - freeSize;
                        result.observe(freeSize, OpenTelemetryMetricsSemanticConventions.STATE_FREE);
                        result.observe(usedSize, OpenTelemetryMetricsSemanticConventions.STATE_USED);
                    })
                    .build();
        }

        if (filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.SYSTEM_PAGING_UTILIZATION).matches()) {
            meter.doubleValueObserverBuilder(OpenTelemetryMetricsSemanticConventions.SYSTEM_PAGING_UTILIZATION)
                    .setDescription("System swap utilization (0.0 to 1.0)")
                    .setUnit("bytes")
                    .setUpdater(result -> {
                        final long freeSize = osBean.getFreeSwapSpaceSize();
                        final long totalSize = osBean.getTotalSwapSpaceSize();
                        final long usedSize = totalSize - freeSize;
                        try {
                            final BigDecimal utilization = new BigDecimal(usedSize)
                                    .divide(new BigDecimal(totalSize), MathContext.DECIMAL64);
                            result.observe(utilization.doubleValue(), Labels.empty());
                        } catch (ArithmeticException e) {
                            result.observe(0, Labels.empty());
                        }
                    })
                    .build();
        }
    }

    private void registerProcessMetrics(com.sun.management.OperatingSystemMXBean osBean) {
        if (filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.PROCESS_CPU_LOAD).matches()) {
            meter.doubleValueObserverBuilder(OpenTelemetryMetricsSemanticConventions.PROCESS_CPU_LOAD)
                    .setDescription("Process CPU load (0.0 to 1.0)")
                    .setUnit("1")
                    .setUpdater(result -> result.observe(osBean.getProcessCpuLoad(), Labels.empty()))
                    .build();
        }

        if (filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.PROCESS_CPU_TIME).matches()) {
            meter.longSumObserverBuilder(OpenTelemetryMetricsSemanticConventions.PROCESS_CPU_TIME)
                    .setDescription("Process CPU time")
                    .setUnit("ns")
                    .setUpdater(result -> result.observe(osBean.getProcessCpuTime(), Labels.empty()))
                    .build();
        }
    }
}
