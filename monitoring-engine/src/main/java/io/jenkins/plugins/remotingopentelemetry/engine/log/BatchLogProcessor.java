/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.remotingopentelemetry.engine.log;

import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class BatchLogProcessor implements LogProcessor {
    private static final String WORKER_THREAD_NAME =
            BatchLogProcessor.class.getSimpleName() + "_WorkerThread";

    private final Worker worker;
    private final Thread workerThread;

    BatchLogProcessor(
            int maxQueueSize,
            long scheduleDelayMillis,
            int maxExportBatchSize,
            long exporterTimeoutMillis,
            LogExporter logExporter) {
        this.worker =
                new Worker(
                        logExporter,
                        scheduleDelayMillis,
                        maxExportBatchSize,
                        exporterTimeoutMillis,
                        new ArrayBlockingQueue<>(maxQueueSize));
        this.workerThread = new DaemonThreadFactory(WORKER_THREAD_NAME).newThread(worker);
        this.workerThread.start();
    }

    public static BatchLogProcessorBuilder builder(LogExporter logExporter) {
        return new BatchLogProcessorBuilder(logExporter);
    }

    @Override
    public void addLogRecord(TaggedLogRecord record) {
        worker.addLogRecord(record);
    }

    @Override
    public CompletableResultCode shutdown() {
        workerThread.interrupt();
        return worker.shutdown();
    }

    @Override
    public CompletableResultCode forceFlush() {
        return worker.forceFlush();
    }

    private static class Worker implements Runnable {
        static {
            Meter meter = GlobalMeterProvider.getMeter("io.opentelemetry.sdk.logging");
            LongCounter logRecordsProcessed =
                    meter
                            .longCounterBuilder("logRecordsProcessed")
                            .setUnit("1")
                            .setDescription("Number of records processed")
                            .build();
            successCounter = logRecordsProcessed.bind(Labels.of("result", "success"));
            exporterFailureCounter =
                    logRecordsProcessed.bind(
                            Labels.of("result", "dropped record", "cause", "exporter failure"));
            queueFullRecordCounter =
                    logRecordsProcessed.bind(Labels.of("result", "dropped record", "cause", "queue full"));
        }

        private static final BoundLongCounter exporterFailureCounter;
        private static final BoundLongCounter queueFullRecordCounter;
        private static final BoundLongCounter successCounter;

        private final long scheduleDelayNanos;
        private final int maxExportBatchSize;
        private final LogExporter logExporter;
        private final long exporterTimeoutMillis;
        private final ArrayList<TaggedLogRecord> batch;
        private final BlockingQueue<TaggedLogRecord> queue;

        private final AtomicReference<CompletableResultCode> flushRequested = new AtomicReference<>();
        private volatile boolean continueWork = true;
        private long nextExportTime;

        private Worker(
                LogExporter logExporter,
                long scheduleDelayMillis,
                int maxExportBatchSize,
                long exporterTimeoutMillis,
                BlockingQueue<TaggedLogRecord> queue) {
            this.logExporter = logExporter;
            this.maxExportBatchSize = maxExportBatchSize;
            this.exporterTimeoutMillis = exporterTimeoutMillis;
            this.scheduleDelayNanos = TimeUnit.MILLISECONDS.toNanos(scheduleDelayMillis);
            this.queue = queue;
            this.batch = new ArrayList<>(this.maxExportBatchSize);
        }

        @Override
        public void run() {
            updateNextExportTime();

            while (continueWork) {
                if (flushRequested.get() != null) {
                    flush();
                }

                try {
                    TaggedLogRecord lastElement = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (lastElement != null) {
                        batch.add(lastElement);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                if (batch.size() >= maxExportBatchSize || System.nanoTime() >= nextExportTime) {
                    exportCurrentBatch();
                    updateNextExportTime();
                }
            }
        }

        private void flush() {
            int recordsToFlush = queue.size();
            while (recordsToFlush > 0) {
                TaggedLogRecord record = queue.poll();
                assert record != null;
                batch.add(record);
                recordsToFlush--;
                if (batch.size() >= maxExportBatchSize) {
                    exportCurrentBatch();
                }
            }
            exportCurrentBatch();
            CompletableResultCode result = flushRequested.get();
            assert result != null;
            flushRequested.set(null);
        }

        private void updateNextExportTime() {
            nextExportTime = System.nanoTime() + scheduleDelayNanos;
        }

        private void exportCurrentBatch() {
            if (batch.isEmpty()) {
                return;
            }

            try {
                final CompletableResultCode result = logExporter.export(batch);
                result.join(exporterTimeoutMillis, TimeUnit.MILLISECONDS);
                if (result.isSuccess()) {
                    successCounter.add(batch.size());
                } else {
                    exporterFailureCounter.add(1);
                }
            } catch (RuntimeException t) {
                exporterFailureCounter.add(batch.size());
            } finally {
                batch.clear();
            }
        }

        private CompletableResultCode shutdown() {
            final CompletableResultCode result = new CompletableResultCode();
            final CompletableResultCode flushResult = forceFlush();
            flushResult.whenComplete(
                    new Runnable() {
                        @Override
                        public void run() {
                            continueWork = false;
                            final CompletableResultCode shutdownResult = logExporter.shutdown();
                            shutdownResult.whenComplete(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if (flushResult.isSuccess() && shutdownResult.isSuccess()) {
                                                result.succeed();
                                            } else {
                                                result.fail();
                                            }
                                        }
                                    });
                        }
                    });
            return result;
        }

        private CompletableResultCode forceFlush() {
            CompletableResultCode flushResult = new CompletableResultCode();
            this.flushRequested.compareAndSet(null, flushResult);
            return this.flushRequested.get();
        }

        public void addLogRecord(TaggedLogRecord record) {
            if (!queue.offer(record)) {
                queueFullRecordCounter.add(1);
            }
        }
    }
}
