package org.scheduler.engine;

/**
 * JobWorker is a consumer that pulls a Job from a shared queue 
 * and executes it. This implements the classic Producer-Consumer pattern.
 */
public class JobWorker implements Runnable {
    
    private final JobQueue queue;

    /**
     * @param queue The shared queue to pull jobs from.
     */
    public JobWorker(JobQueue queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            // take() is a BLOCKING call. 
            // If the queue is empty, this thread will sleep until a job is added.
            Job<?> job = queue.take();
            
            // Execute the job's logic.
            job.run();
        } catch (InterruptedException e) {
            // If the thread is interrupted while waiting, we should stop.
            Thread.currentThread().interrupt();
        }
    }
}
