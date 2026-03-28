package org.scheduler.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * BenchmarkRunner compares performance across different thread counts
 * and provides detailed latency percentiles.
 */
public class BenchmarkRunner {

    private static final int JOB_COUNT = 500;
    private static final int WORKLOAD_MS = 10;
    private static final int QUEUE_CAPACITY = 100;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Benchmark...");
        System.out.println("Job Count: " + JOB_COUNT);
        System.out.println("Workload: " + WORKLOAD_MS + "ms per job");
        System.out.println("Queue Capacity: " + QUEUE_CAPACITY);
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf("%-10s | %-15s | %-10s | %-10s | %-10s | %-10s | %-10s\n", 
                          "Threads", "Throughput", "Avg Lat", "p50 Lat", "p95 Lat", "p99 Lat", "Peak Q");
        System.out.println("----------------------------------------------------------------------------------");

        // Sequential Baseline
        runSequential();

        // Parallel Scaling
        int[] threadCounts = {1, 2, 4, 8, 16, 32};
        for (int threads : threadCounts) {
            runParallel(threads);
        }
        System.out.println("----------------------------------------------------------------------------------");
    }

    private static void runSequential() {
        long startSeq = System.nanoTime();
        for (int i = 0; i < JOB_COUNT; i++) {
            simulateWork();
        }
        long endSeq = System.nanoTime();
        double seqDurationSec = (endSeq - startSeq) / 1_000_000_000.0;
        double seqThroughput = JOB_COUNT / seqDurationSec;

        System.out.printf("%-10s | %-15.2f | %-10.2f | %-10s | %-10s | %-10s | %-10s\n", 
                          "SEQ (1)", seqThroughput, (double)WORKLOAD_MS, "-", "-", "-", "-");
    }

    private static void runParallel(int threadCount) throws Exception {
        JobEngine engine = new JobEngine(threadCount, threadCount, 60, QUEUE_CAPACITY);
        List<Job<Void>> jobs = new ArrayList<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long startPar = System.nanoTime();
        for (int i = 0; i < JOB_COUNT; i++) {
            Job<Void> job = new Job<>("Job-" + i, JobPriority.MEDIUM, () -> {
                simulateWork();
                return null;
            });
            jobs.add(job);
            futures.add(engine.submit(job));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long endPar = System.nanoTime();
        double parDurationSec = (endPar - startPar) / 1_000_000_000.0;
        double parThroughput = JOB_COUNT / parDurationSec;

        // Collect latencies
        List<Long> latencies = new ArrayList<>();
        long totalLat = 0;
        for (Job<Void> job : jobs) {
            long lat = job.getLatencyMs();
            latencies.add(lat);
            totalLat += lat;
        }
        Collections.sort(latencies);

        double avgLat = (double) totalLat / JOB_COUNT;
        long p50 = latencies.get((int) (JOB_COUNT * 0.50));
        long p95 = latencies.get((int) (JOB_COUNT * 0.95));
        long p99 = latencies.get((int) (JOB_COUNT * 0.99));

        System.out.printf("%-10d | %-15.2f | %-10.2f | %-10d | %-10d | %-10d | %-10d\n", 
                          threadCount, parThroughput, avgLat, p50, p95, p99, engine.getMetrics().getPeakQueueDepth());

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
