package org.scheduler.engine;

/**
 * JobWorker is a consumer that pulls a Job from a shared queue 
 * and executes it. This implements the classic Producer-Consumer pattern.
 */
public class JobWorker implements Runnable {
    
    private final JobQueue queue;
    private final MetricsCollector metrics;

    /**
     * @param queue The shared queue to pull jobs from.
     * @param metrics The metrics collector to record performance.
     */
    public JobWorker(JobQueue queue, MetricsCollector metrics) {
        this.queue = queue;
        this.metrics = metrics;
    }

    @Override
    public void run() {
        try {
            // take() is a BLOCKING call. 
            // If the queue is empty, this thread will sleep until a job is added.
            Job<?> job = queue.take();
            
            // Execute the job's logic.
            job.run();
            
            // Record latency.
            if (metrics != null) {
                long latency = System.currentTimeMillis() - job.getCreatedAt();
                metrics.recordLatency(latency);
            }
        } catch (InterruptedException e) {
            // If the thread is interrupted while waiting, we should stop.
            Thread.currentThread().interrupt();
        }
    }
}
