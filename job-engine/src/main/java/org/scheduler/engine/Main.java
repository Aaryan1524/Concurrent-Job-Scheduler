package org.scheduler.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Demonstration of the Concurrent Job Scheduler.
 */
public class Main {
    public static void main(String[] args) {
        // Initialize the engine with 4 core threads and 8 max threads.
        JobEngine engine = new JobEngine(4, 8, 30L);
        Random random = new Random();

        List<CompletableFuture<String>> results = new ArrayList<>();

        System.out.println("--- Submitting 10 Jobs with different priorities ---");

        // Submit 10 jobs with varying priorities.
        for (int i = 1; i <= 10; i++) {
            final int jobId = i;
            
            // Randomly assign a priority to demonstrate scheduling.
            JobPriority priority = JobPriority.values()[random.nextInt(JobPriority.values().length)];
            
            Job<String> job = new Job<>("Job-" + jobId, priority, () -> {
                // Simulate some time-consuming work.
                int sleepTime = 500 + random.nextInt(1500);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                
                return String.format("Result from %s (Priority: %s) after %dms", 
                                     "Job-" + jobId, priority, sleepTime);
            });

            System.out.printf("Submitted: %s at %dms\n", job, System.currentTimeMillis() % 10000);
            results.add(engine.submit(job));
        }

        // Wait for all jobs to complete and print results.
        System.out.println("\n--- Processing Results (Results arrive as they finish) ---");
        
        CompletableFuture.allOf(results.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                results.forEach(f -> {
                    try {
                        System.out.println("COMPLETED: " + f.get());
                    } catch (Exception e) {
                        System.err.println("Job failed: " + e.getMessage());
                    }
                });
                
                // Cleanup
                engine.shutdown();
                System.out.println("\n--- Job Engine Shutdown ---");
            }).join(); // Wait for all to finish before exiting main.
    }
}
