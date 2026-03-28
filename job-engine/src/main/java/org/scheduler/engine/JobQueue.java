package org.scheduler.engine;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * JobQueue is a wrapper around a PriorityBlockingQueue for Job objects.
 * It provides a clean interface for adding and taking jobs, ensuring that 
 * high-priority jobs are served first.
 */
public class JobQueue {
    
    private final PriorityBlockingQueue<Job<?>> internalQueue;

    public JobQueue() {
        this.internalQueue = new PriorityBlockingQueue<>();
    }

    /**
     * Adds a job to the queue.
     * @param job The job to add.
     */
    public void put(Job<?> job) {
        internalQueue.put(job);
    }

    /**
     * Retrieves and removes the head of the queue, waiting if necessary 
     * until an element becomes available.
     * @return The next job to execute.
     * @throws InterruptedException if interrupted while waiting.
     */
    public Job<?> take() throws InterruptedException {
        return internalQueue.take();
    }

    /**
     * Returns the number of jobs currently in the queue.
     * @return The size of the queue.
     */
    public int size() {
        return internalQueue.size();
    }
}
