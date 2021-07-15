package io.jenkins.plugins.remotingopentelemetry.engine.metric;

import io.jenkins.plugins.remotingopentelemetry.engine.semconv.OpenTelemetryMetricsSemanticConventions;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.math.BigDecimal;
import java.math.MathContext;

public class OperatingSystemMXBeanMetric {
    private final Meter meter;
    private final boolean skipSystemMetrics;
    private final boolean skipProcessMetrics;

    public OperatingSystemMXBeanMetric(SdkMeterProvider sdkMeterProvider) {
        this(sdkMeterProvider, true, true);
    }

    public OperatingSystemMXBeanMetric(
            SdkMeterProvider sdkMeterProvider,
            boolean useSystemMetrics,
            boolean useProcessMetrics) {
        this.skipSystemMetrics = !useSystemMetrics;
        this.skipProcessMetrics = !useProcessMetrics;
        meter = sdkMeterProvider.get(OperatingSystemMXBean.class.getName());
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
        if (skipSystemMetrics) return;
        meter.doubleValueObserverBuilder(OpenTelemetryMetricsSemanticConventions.SYSTEM_CPU_LOAD_AVERAGE_1M)
                .setDescription("System CPU load average 1 minutes")
                .setUpdater(result -> result.observe(operatingSystemMXBean.getSystemLoadAverage(), Labels.empty()))
                .build();
    }

    private void registerSystemMetrics(com.sun.management.OperatingSystemMXBean osBean) {
        if (skipSystemMetrics) return;
        meter.doubleValueObserverBuilder(OpenTelemetryMetricsSemanticConventions.SYSTEM_CPU_LOAD_AVERAGE_1M)
                .setDescription("System CPU load average 1 minutes")
                .setUpdater(result -> result.observe(osBean.getSystemLoadAverage(), Labels.empty()))
                .build();

        meter.doubleValueObserverBuilder(OpenTelemetryMetricsSemanticConventions.SYSTEM_CPU_LOAD)
                .setDescription("System CPU load (0.0 to 1.0)")
                .setUnit("1")
                .setUpdater(result -> result.observe(osBean.getSystemCpuLoad(), Labels.empty()))
                .build();

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

    private void registerProcessMetrics(com.sun.management.OperatingSystemMXBean osBean) {
        if (skipProcessMetrics) return;
        meter.doubleValueObserverBuilder(OpenTelemetryMetricsSemanticConventions.PROCESS_CPU_LOAD)
                .setDescription("Process CPU load (0.0 to 1.0)")
                .setUnit("1")
                .setUpdater(result -> result.observe(osBean.getProcessCpuLoad(), Labels.empty()))
                .build();

        meter.longSumObserverBuilder(OpenTelemetryMetricsSemanticConventions.PROCESS_CPU_TIME)
                .setDescription("Process CPU time")
                .setUnit("ns")
                .setUpdater(result -> result.observe(osBean.getProcessCpuTime(), Labels.empty()))
                .build();
    }
}
