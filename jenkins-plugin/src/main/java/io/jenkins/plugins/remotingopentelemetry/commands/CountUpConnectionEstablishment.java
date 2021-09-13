package io.jenkins.plugins.remotingopentelemetry.commands;

import io.jenkins.plugins.remotingopentelemetry.engine.metric.ConnectionEstablishmentsCountMetric;
import io.opentelemetry.api.metrics.LongCounter;
import jenkins.security.MasterToSlaveCallable;

public class CountUpConnectionEstablishment extends MasterToSlaveCallable<Void, RuntimeException> {
    @Override
    public Void call() throws RuntimeException {
        LongCounter counter = ConnectionEstablishmentsCountMetric.getCounter();
        if (counter != null) counter.add(1);
        return null;
    }
}
