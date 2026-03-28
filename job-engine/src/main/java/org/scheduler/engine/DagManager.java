package org.scheduler.engine;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * DagManager handles the execution of jobs with dependencies.
 * it ensures that a job only starts after all its predecessors have completed.
 */
public class DagManager {
    
    private final JobEngine engine;

    public DagManager(JobEngine engine) {
        this.engine = engine;
    }

    /**
     * Executes a list of jobs, respecting their dependencies.
     * @param jobs The jobs to execute.
     */
    public void execute(List<Job<?>> jobs) {
        for (Job<?> job : jobs) {
            List<Job<?>> predecessors = job.getPredecessors();
            
            if (predecessors.isEmpty()) {
                // No dependencies, submit immediately.
                engine.submit(job);
            } else {
                // Wait for all predecessors to complete before submitting this job.
                CompletableFuture<?>[] predecessorFutures = predecessors.stream()
                        .map(Job::getFuture)
                        .toArray(CompletableFuture[]::new);

                CompletableFuture.allOf(predecessorFutures)
                        .thenRunAsync(() -> engine.submit(job));
            }
        }
    }
}
