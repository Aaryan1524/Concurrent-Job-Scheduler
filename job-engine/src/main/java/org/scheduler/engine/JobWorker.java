package org.scheduler.engine;

/**
 * JobWorker is a consumer that pulls a Job from a shared queue 
 * and executes it. This implements the classic Producer-Consumer pattern.
 */
public class JobWorker implements Runnable {
    
    private final JobQueue queue;
    private final MetricsCollector metrics;
    private final Job<?> submittedJob; // The job that triggered this worker

    /**
     * @param queue The shared queue to pull jobs from.
     * @param metrics The metrics collector to record performance.
     * @param submittedJob The job that triggered this worker (for tracking).
     */
    public JobWorker(JobQueue queue, MetricsCollector metrics, Job<?> submittedJob) {
        this.queue = queue;
        this.metrics = metrics;
        this.submittedJob = submittedJob;
    }

    @Override
    public void run() {
        try {
            // take() is a BLOCKING call. 
            // If the queue is empty, this thread will sleep until a job is added.
            Job<?> job = queue.take();
            
            job.markStarted();
            // Execute the job's logic.
            job.run();
            job.markCompleted();
            
            // Record latency.
            if (metrics != null) {
                metrics.recordLatency(job.getLatencyMs());
            }
        } catch (InterruptedException e) {
            // If the thread is interrupted while waiting, we should stop.
            Thread.currentThread().interrupt();
        }
    }

    public Job<?> getSubmittedJob() {
        return submittedJob;
    }
}
