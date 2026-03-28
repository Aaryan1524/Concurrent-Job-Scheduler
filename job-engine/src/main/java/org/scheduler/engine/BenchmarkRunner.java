package org.scheduler.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * BenchmarkRunner compares sequential vs parallel execution performance.
 */
public class BenchmarkRunner {

    private static final int JOB_COUNT = 1000;
    private static final int WORKLOAD_MS = 10;
    private static final int THREAD_COUNT = 8;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Benchmark...");
        System.out.println("Job Count: " + JOB_COUNT);
        System.out.println("Workload: " + WORKLOAD_MS + "ms per job");
        System.out.println("Thread Count: " + THREAD_COUNT);
        System.out.println("----------------------------------------");

        // 1. Sequential Baseline
        long startSeq = System.nanoTime();
        for (int i = 0; i < JOB_COUNT; i++) {
            simulateWork();
        }
        long endSeq = System.nanoTime();
        double seqDurationSec = (endSeq - startSeq) / 1_000_000_000.0;
        double seqThroughput = JOB_COUNT / seqDurationSec;

        System.out.printf("Sequential Time: %.3f s\n", seqDurationSec);
        System.out.printf("Sequential Throughput: %.2f jobs/sec\n", seqThroughput);
        System.out.println("----------------------------------------");

        // 2. Parallel Execution (JobEngine)
        JobEngine engine = new JobEngine(THREAD_COUNT, THREAD_COUNT, 60);
        List<CompletableFuture<?>> futures = new ArrayList<>();

        long startPar = System.nanoTime();
        for (int i = 0; i < JOB_COUNT; i++) {
            futures.add(engine.submit(new Job<>("Job-" + i, JobPriority.MEDIUM, () -> {
                simulateWork();
                return null;
            })));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long endPar = System.nanoTime();
        double parDurationSec = (endPar - startPar) / 1_000_000_000.0;
        double parThroughput = JOB_COUNT / parDurationSec;

        MetricsCollector metrics = engine.getMetrics();

        System.out.printf("Parallel Time: %.3f s\n", parDurationSec);
        System.out.printf("Parallel Throughput: %.2f jobs/sec\n", parThroughput);
        System.out.printf("Average Latency: %.2f ms\n", metrics.getAverageLatencyMs());
        System.out.printf("Peak Queue Depth: %d\n", metrics.getPeakQueueDepth());
        System.out.println("----------------------------------------");

        // 3. Comparison
        double speedup = parThroughput / seqThroughput;
        System.out.printf("Speedup: %.2fx\n", speedup);

        engine.shutdown();
    }

    private static void simulateWork() {
        try {
            Thread.sleep(WORKLOAD_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
