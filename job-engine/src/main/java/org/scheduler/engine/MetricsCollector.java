package org.scheduler.engine;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;

/**
 * MetricsCollector tracks performance metrics of the JobEngine.
 */
public class MetricsCollector {
    
    private final AtomicLong totalLatency = new AtomicLong(0);
    private final AtomicLong completedJobs = new AtomicLong(0);
    private final LongAccumulator peakQueueDepth = new LongAccumulator(Math::max, 0);

    /**
     * Records the latency of a completed job.
     * @param latencyMs Latency in milliseconds.
     */
    public void recordLatency(long latencyMs) {
        totalLatency.addAndGet(latencyMs);
        completedJobs.incrementAndGet();
    }

    /**
     * Records the current depth of the queue to track the peak.
     * @param currentDepth Current number of jobs in the queue.
     */
    public void recordQueueDepth(int currentDepth) {
        peakQueueDepth.accumulate(currentDepth);
    }

    public double getAverageLatencyMs() {
        long count = completedJobs.get();
        return count == 0 ? 0 : (double) totalLatency.get() / count;
    }

    public long getPeakQueueDepth() {
        return peakQueueDepth.get();
    }

    public long getCompletedJobs() {
        return completedJobs.get();
    }

    public void reset() {
        totalLatency.set(0);
        completedJobs.set(0);
        peakQueueDepth.reset();
    }
}
