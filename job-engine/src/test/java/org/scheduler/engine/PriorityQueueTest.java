package org.scheduler.engine;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PriorityQueueTest {

    @Test
    public void testPriorityOrdering() throws Exception {
        // Use 1 thread to ensure sequential execution from the queue.
        JobEngine engine = new JobEngine(1, 1, 0);
        List<String> executionOrder = new CopyOnWriteArrayList<>();

        // 1. Submit a blocker job to fill the single thread and let the queue build up.
        CompletableFuture<Void> blockerFinished = engine.submit(new Job<>("Blocker", JobPriority.URGENT, () -> {
            Thread.sleep(500); // Hold the thread for 500ms
            return null;
        }));

        // 2. Submit 5 LOW priority jobs.
        List<CompletableFuture<?>> lowJobs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final int id = i;
            lowJobs.add(engine.submit(new Job<>("LOW-" + id, JobPriority.LOW, () -> {
                executionOrder.add("LOW-" + id);
                return null;
            })));
        }

        // 3. Submit 3 HIGH priority jobs.
        List<CompletableFuture<?>> highJobs = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            final int id = i;
            highJobs.add(engine.submit(new Job<>("HIGH-" + id, JobPriority.HIGH, () -> {
                executionOrder.add("HIGH-" + id);
                return null;
            })));
        }

        // Wait for all jobs to complete.
        blockerFinished.get(5, TimeUnit.SECONDS);
        CompletableFuture.allOf(lowJobs.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);
        CompletableFuture.allOf(highJobs.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);

        engine.shutdown();

        // 4. Verify that all 3 HIGH jobs executed BEFORE any LOW jobs (except if any started before they were all submitted).
        // Since we used a blocker, all 8 jobs (5 LOW, 3 HIGH) should have been in the priorityQueue 
        // when the blocker finished.
        
        System.out.println("Execution Order: " + executionOrder);

        // The first 3 should be HIGH jobs.
        for (int i = 0; i < 3; i++) {
            assertTrue(executionOrder.get(i).startsWith("HIGH"), 
                "Expected HIGH job at index " + i + " but got " + executionOrder.get(i));
        }

        // The next 5 should be LOW jobs.
        for (int i = 3; i < 8; i++) {
            assertTrue(executionOrder.get(i).startsWith("LOW"), 
                "Expected LOW job at index " + i + " but got " + executionOrder.get(i));
        }
    }
}
