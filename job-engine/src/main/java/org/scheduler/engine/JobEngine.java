package org.scheduler.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * JobEngine manages the lifecycle of concurrent jobs.
 * It uses a ThreadPoolExecutor as its core engine and a 
 * PriorityBlockingQueue to ensure high-priority jobs go first.
 */
public class JobEngine {
    
    // The internal queue that sorts our jobs.
    private final JobQueue priorityQueue;
    
    // The executor pool that provides worker threads.
    private final ThreadPoolExecutor executor;

    // Metrics collector to track performance.
    private final MetricsCollector metrics = new MetricsCollector();

    // Dead-letter list for rejected jobs.
    private final List<Job<?>> deadLetterJobs = new CopyOnWriteArrayList<>();

    /**
     * @param corePoolSize The base number of worker threads.
     * @param maxPoolSize The ceiling of worker threads.
     * @param keepAliveTimeSeconds How long to keep extra threads alive.
     */
    public JobEngine(int corePoolSize, int maxPoolSize, long keepAliveTimeSeconds) {
        this.priorityQueue = new JobQueue();
        
        // We use a LinkedBlockingQueue for the executor's internal task queue.
        // This queue will hold our JobWorker tasks.
        this.executor = new ThreadPoolExecutor(
                corePoolSize, 
                maxPoolSize, 
                keepAliveTimeSeconds, 
                TimeUnit.SECONDS, 
                new LinkedBlockingQueue<>(),
                new JobRejectionHandler()
        );
    }

    /**
     * Submits a job to the engine for asynchronous execution.
     * 
     * @param job The job to submit.
     * @return A CompletableFuture that completes when the job finishes.
     */
    public <V> CompletableFuture<V> submit(Job<V> job) {
        // Step 1: Add the job to our priority-aware queue.
        priorityQueue.put(job);
        
        // Track peak queue depth.
        metrics.recordQueueDepth(priorityQueue.size());
        
        // Step 2: Signal the executor to run a JobWorker.
        // When the pool is ready, a JobWorker will start and take() 
        // the highest priority job from priorityQueue.
        executor.execute(new JobWorker(priorityQueue, metrics, job));
        
        // Return the future so the caller can track completion.
        return job.getFuture();
    }

    public MetricsCollector getMetrics() {
        return metrics;
    }

    public List<Job<?>> getDeadLetterJobs() {
        return Collections.unmodifiableList(new ArrayList<>(deadLetterJobs));
    }

    /**
     * Custom RejectionHandler to capture jobs that the executor cannot handle.
     */
    private class JobRejectionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (r instanceof JobWorker) {
                JobWorker worker = (JobWorker) r;
                Job<?> job = worker.getSubmittedJob();
                
                if (job != null) {
                    System.err.println("Rejected Job: " + job.getName() + " (ID: " + job.getJobId() + ")");
                    
                    // Remove from priority queue since it won't be picked up
                    priorityQueue.remove(job);
                    
                    // Add to dead letter list
                    deadLetterJobs.add(job);
                    
                    // Fail the future so the caller knows it was rejected
                    job.getFuture().completeExceptionally(new RejectedExecutionException("Job rejected by engine"));
                }
            }
        }
    }

    /**
     * Returns true if the engine has been shut down.
     * @return true if shut down.
     */
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    /**
     * Shuts down the engine gracefully using a default 60-second timeout.
     */
    public void shutdown() {
        shutdown(60, TimeUnit.SECONDS);
    }

    /**
     * Shuts down the engine gracefully with a configurable timeout.
     * @param timeout The maximum time to wait for termination.
     * @param unit The time unit of the timeout argument.
     */
    public void shutdown(long timeout, TimeUnit unit) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout, unit)) {
                System.err.println("Termination timed out. Forcing shutdown.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
