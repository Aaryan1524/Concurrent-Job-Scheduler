package org.scheduler.engine;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DagManagerTest {

    @Test
    public void testJobChaining() throws Exception {
        JobEngine engine = new JobEngine(2, 4, 60);
        DagManager dagManager = new DagManager(engine);
        List<String> executionOrder = new CopyOnWriteArrayList<>();

        // 1. Fetch Data
        Job<String> fetchData = new Job<>("FetchData", JobPriority.HIGH, () -> {
            String result = "Raw Data";
            System.out.println("Step 1: Fetching data -> " + result);
            executionOrder.add("fetch");
            return result;
        });

        // 2. Process Data (Depends on Fetch Data)
        Job<String> processData = new Job<>("ProcessData", JobPriority.MEDIUM, () -> {
            String input = fetchData.getFuture().join();
            System.out.println("Step 2: Processing input -> " + input);
            String result = input + " (Processed)";
            executionOrder.add("process");
            return result;
        });
        processData.dependsOn(fetchData);

        // 3. Send Result (Depends on Process Data)
        Job<Void> sendResult = new Job<>("SendResult", JobPriority.LOW, () -> {
            String input = processData.getFuture().join();
            System.out.println("Step 3: Sending result -> " + input);
            executionOrder.add("send");
            return null;
        });
        sendResult.dependsOn(processData);

        // Execute the DAG
        dagManager.execute(Arrays.asList(fetchData, processData, sendResult));

        // Wait for final job to complete
        sendResult.getFuture().get(5, TimeUnit.SECONDS);

        engine.shutdown();

        // Verify execution order
        assertEquals(Arrays.asList("fetch", "process", "send"), executionOrder);
    }
}
