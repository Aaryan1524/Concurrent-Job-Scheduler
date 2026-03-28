# Concurrent Job Scheduler

A high-performance, priority-aware concurrent job execution engine for Java.

## Features
- **Priority-Aware Execution**: Uses a `PriorityBlockingQueue` to ensure high-priority jobs are processed first.
- **DAG Support**: `DagManager` allows for complex job dependencies using `CompletableFuture` chaining.
- **Backpressure & Flow Control**: Prevents system overload by blocking producers when the queue reaches capacity.
- **Performance Monitoring**: Integrated `MetricsCollector` tracks latency percentiles and peak queue depth.
- **Graceful Shutdown**: Configurable timeout-based shutdown to ensure in-flight jobs finish safely.
- **Dead-Letter Handling**: A `RejectionHandler` captures and stores jobs that couldn't be executed.

## Benchmark Results (Actual)

Testing with **500 jobs**, each simulating a **10ms workload**, with a **Queue Capacity of 100**.

| Threads | Throughput (jobs/sec) | Avg Latency (ms) | p50 Latency | p95 Latency | p99 Latency | Peak Queue |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **SEQ (1)** | 84.79 | 10.00 | - | - | - | - |
| 1 | 83.85 | 1088.93 | 1209 | 1232 | 1232 | 100 |
| 2 | 167.85 | 549.90 | 610 | 619 | 622 | 100 |
| 4 | 338.10 | 277.93 | 309 | 314 | 316 | 100 |
| 8 | 673.89 | **143.95** | 160 | 165 | 167 | 100 |
| 16 | 1346.57 | 75.87 | 84 | 89 | 92 | 100 |
| 32 | **2676.50** | 42.66 | 46 | 54 | 57 | 100 |

### Performance Analysis
- **Scalability**: Throughput scales almost linearly with thread count up to 32 threads, demonstrating efficient resource utilization.
- **Backpressure**: By limiting the queue capacity to 100, we've successfully implemented backpressure. At 8 threads, the average latency is now ~144ms, compared to ~735ms without backpressure. This ensures that jobs spend less time waiting in the queue and prevents the producer from overwhelming the consumer.
- **Latency Control**: Latency percentiles (p50, p95, p99) are tightly clustered, indicating predictable performance under load.

## How to Run Benchmarks
```bash
# Compile the project
mvn clean install

# Run BenchmarkRunner
java -cp job-engine/target/classes org.scheduler.engine.BenchmarkRunner
```

## How to Run Tests
```bash
# Run all tests
mvn test
```
