package org.scheduler.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * Job represents a single unit of work that can be executed concurrently.
 * V is the result type of the job.
 * It implements Runnable to allow execution by a worker.
 * It implements Comparable for priority-based ordering.
 */
public class Job<V> implements Runnable, Comparable<Job<V>> {
    
    private final String jobId;
    private final String name;
    private final JobPriority priority;
    private final long createdAt;
    private final List<Job<?>> predecessors = new ArrayList<>();
    
    // The actual work to be done.
    private final Callable<V> task;
    
    // The future result that will be completed when the task is done.
    private final CompletableFuture<V> future;

    /**
     * Constructs a new Job with a Callable task.
     * 
     * @param name The name of the job.
     * @param priority The priority level.
     * @param task The task logic.
     */
    public Job(String name, JobPriority priority, Callable<V> task) {
        this.jobId = UUID.randomUUID().toString();
        this.name = name;
        this.priority = priority;
        this.createdAt = System.currentTimeMillis();
        this.task = task;
        this.future = new CompletableFuture<>();
    }

    /**
     * Defines that this job depends on the completion of the provided jobs.
     * @param predecessors The jobs that must complete before this one starts.
     * @return This job for method chaining.
     */
    public Job<V> dependsOn(Job<?>... predecessors) {
        this.predecessors.addAll(Arrays.asList(predecessors));
        return this;
    }

    public List<Job<?>> getPredecessors() {
        return predecessors;
    }

    /**
     * Executes the task and completes the future.
     */
    @Override
    public void run() {
        try {
            // Execute the callable task.
            V result = task.call();
            // Signal success and deliver the result.
            future.complete(result);
        } catch (Exception e) {
            // Signal failure.
            future.completeExceptionally(e);
        }
    }

    public CompletableFuture<V> getFuture() {
        return future;
    }

    @Override
    public int compareTo(Job<V> other) {
        int priorityCompare = Integer.compare(this.priority.getLevel(), other.priority.getLevel());
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        return Long.compare(this.createdAt, other.createdAt);
    }

    public String getJobId() {
        return jobId;
    }

    public String getName() {
        return name;
    }

    public JobPriority getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return String.format("Job[id=%s, name=%s, priority=%s]", jobId, name, priority);
    }
}
