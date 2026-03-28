package org.scheduler.engine;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * JobQueue is a wrapper around a PriorityBlockingQueue for Job objects.
 * It provides a clean interface for adding and taking jobs, ensuring that 
 * high-priority jobs are served first.
 * Now includes a Semaphore to provide backpressure when a capacity is reached.
 */
public class JobQueue {
    
    private final PriorityBlockingQueue<Job<?>> internalQueue;
    private final Semaphore capacitySemaphore;
    private final int capacity;

    public JobQueue(int capacity) {
        this.capacity = capacity;
        this.internalQueue = new PriorityBlockingQueue<>();
        this.capacitySemaphore = new Semaphore(capacity);
    }

    /**
     * Adds a job to the queue. Blocks if capacity is reached.
     * @param job The job to add.
     * @throws InterruptedException if interrupted while waiting.
     */
    public void put(Job<?> job) throws InterruptedException {
        capacitySemaphore.acquire();
        internalQueue.put(job);
    }

    /**
     * Retrieves and removes the head of the queue, waiting if necessary 
     * until an element becomes available.
     * @return The next job to execute.
     * @throws InterruptedException if interrupted while waiting.
     */
    public Job<?> take() throws InterruptedException {
        Job<?> job = internalQueue.take();
        capacitySemaphore.release();
        return job;
    }

    /**
     * Removes a specific job from the queue if it exists.
     * @param job The job to remove.
     * @return true if removed, false otherwise.
     */
    public boolean remove(Job<?> job) {
        if (internalQueue.remove(job)) {
            capacitySemaphore.release();
            return true;
        }
        return false;
    }

    /**
     * Returns the number of jobs currently in the queue.
     * @return The size of the queue.
     */
    public int size() {
        return internalQueue.size();
    }

    public int getCapacity() {
        return capacity;
    }
}
