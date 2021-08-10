package io.jenkins.plugins.remotingopentelemetry.engine.metric;

import io.jenkins.plugins.remotingopentelemetry.engine.semconv.OpenTelemetryMetricsSemanticConventions;
import io.opentelemetry.api.metrics.AsynchronousInstrument.LongResult;
import io.opentelemetry.api.metrics.AsynchronousInstrument.DoubleResult;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * TODO: Windows and Mac support
 *
 * Collect system level filesystem metrics
 */
public class FilesystemMetric {
    private static final Logger LOGGER = Logger.getLogger(FilesystemMetric.class.getName());

    private final Meter meter;
    private final Pattern filterPattern;

    public FilesystemMetric(SdkMeterProvider sdkMeterProvider, Pattern filterPattern) {
        meter = sdkMeterProvider.get(FilesystemMetric.class.getName());
        this.filterPattern = filterPattern;
    }

    public void register() {
        boolean filesystemUsageEnabled
                = filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.SYSTEM_FILESYSTEM_USAGE).matches();
        boolean filesystemUtilizationEnabled
                = filterPattern.matcher(OpenTelemetryMetricsSemanticConventions.SYSTEM_FILESYSTEM_UTILIZATION).matches();

        if (filesystemUsageEnabled && filesystemUtilizationEnabled && MountInfo.createFromProcMounts() == null) {
            LOGGER.log(Level.INFO, "Failed to observe filesystem, don't start monitoring filesystem");
            return;
        }

        if (filesystemUsageEnabled) {
            meter.longUpDownSumObserverBuilder(OpenTelemetryMetricsSemanticConventions.SYSTEM_FILESYSTEM_USAGE)
                    .setDescription("System filesystem usage")
                    .setUnit("bytes")
                    .setUpdater(this::observeFilesystemUsage)
                    .build();
        }

        if (filesystemUtilizationEnabled) {
            meter.doubleValueObserverBuilder(OpenTelemetryMetricsSemanticConventions.SYSTEM_FILESYSTEM_UTILIZATION)
                    .setDescription("System filesystem utilization (0.0 to 1.0)")
                    .setUnit("1")
                    .setUpdater(this::observeFilesystemUtilization)
                    .build();
        }
    }

    private void observeFilesystemUtilization(DoubleResult result) {
        List<MountInfo> mountInfoList = MountInfo.createFromProcMounts();
        if (mountInfoList == null) return;

        for (MountInfo mountInfo : mountInfoList) {
            result.observe(
                    mountInfo.getUsedRatio(),
                    Labels.builder()
                            .put("device", mountInfo.getDevice())
                            .put("state", "used")
                            .put("type", mountInfo.getType())
                            .put("mode", mountInfo.getMode())
                            .put("mountpoint", mountInfo.getMountPoint())
                            .build()
            );
            result.observe(
                    mountInfo.getFreeRatio(),
                    Labels.builder()
                            .put("device", mountInfo.getDevice())
                            .put("state", "free")
                            .put("type", mountInfo.getType())
                            .put("mode", mountInfo.getMode())
                            .put("mountpoint", mountInfo.getMountPoint())
                            .build()
            );
        }
    }

    private void observeFilesystemUsage(LongResult result) {
        List<MountInfo> mountInfoList = MountInfo.createFromProcMounts();
        if (mountInfoList == null) return;

        for (MountInfo mountInfo : mountInfoList) {
            result.observe(
                    mountInfo.getUsedBytes(),
                    Labels.builder()
                            .put("device", mountInfo.getDevice())
                            .put("state", "used")
                            .put("type", mountInfo.getType())
                            .put("mode", mountInfo.getMode())
                            .put("mountpoint", mountInfo.getMountPoint())
                            .build()
            );
            result.observe(
                    mountInfo.getFreeBytes(),
                    Labels.builder()
                            .put("device", mountInfo.getDevice())
                            .put("state", "free")
                            .put("type", mountInfo.getType())
                            .put("mode", mountInfo.getMode())
                            .put("mountpoint", mountInfo.getMountPoint())
                            .build()
            );
        }
    }

    /*package*/static class MountInfo {
        /**
         * Create a list of MountInfo from /proc/mounts
         * @return null when failed to get data from /proc/mounts
         */
        @Nullable
        static List<MountInfo> createFromProcMounts() {
            List<String> lines;
            List<MountInfo> mountInfoList = new ArrayList<>();
            try {
                lines = Files.lines(Paths.get("/proc/mounts"), StandardCharsets.UTF_8).collect(Collectors.toList());
            } catch (IOException e) {
                return null;
            }

            for (String entry : lines) {
                if (entry.startsWith("#") || entry.trim().equals("")) continue;
                String[] columns = entry.trim().split("\\s+");

                if (columns.length != 6) continue;

                String device = columns[0];
                String mountPoint = columns[1];
                String type = columns[2];
                String option = columns[3];
                // we don't need to use column[4] and [5]

                long free;
                long total;
                try {
                    File file = new File(mountPoint);
                    free = file.getUsableSpace();
                    total = file.getTotalSpace();
                } catch (Throwable e) {
                    continue;
                }

                mountInfoList.add(new MountInfo(device, type, option, mountPoint, free, total));
            }
            if (mountInfoList.size() == 0) return null;
            return mountInfoList;
        }

        private final String device;
        private final String type;
        private final String mode;
        private final String mountPoint;
        private final long free;
        private final long total;

        private MountInfo(String device, String type, String mode, String mountPoint, long free, long total) {
            this.device = device;
            this.type = type;
            this.mode = mode;
            this.mountPoint = mountPoint;
            this.free = free;
            this.total = total;
        }

        public String getDevice() {
            return device;
        }

        public String getType() {
            return type;
        }

        public String getMode() {
            return mode;
        }

        public String getMountPoint() {
            return mountPoint;
        }

        public long getUsedBytes() {
            return total - free;
        }

        public long getFreeBytes() {
            return free;
        }

        public double getUsedRatio() {
            return ((double) (total - free)) / (double) total;
        }

        public double getFreeRatio() {
            return ((double) free) / (double) total;
        }
    }
}
