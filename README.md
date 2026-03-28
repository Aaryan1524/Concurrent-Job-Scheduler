# Concurrent Job Scheduler

A high-performance, priority-aware concurrent job execution engine for Java.

## Features
- **Priority-Aware Execution**: Uses a `PriorityBlockingQueue` to ensure high-priority jobs are processed first.
- **DAG Support**: `DagManager` allows for complex job dependencies using `CompletableFuture` chaining.
- **Performance Monitoring**: Integrated `MetricsCollector` tracks average latency and peak queue depth.
- **Graceful Shutdown**: Configurable timeout-based shutdown to ensure in-flight jobs finish safely.
- **Dead-Letter Handling**: A `RejectionHandler` captures and stores jobs that couldn't be executed.

## Benchmark Results (Actual)

Testing on **8 worker threads** with **1000 jobs**, each simulating a **10ms workload**.

| Metric | Sequential Baseline | Parallel JobEngine |
| :--- | :--- | :--- |
| **Total Wall Time** | 11.881 s | 1.502 s |
| **Throughput** | 84.17 jobs/sec | 665.69 jobs/sec |
| **Speedup** | 1.00x | **7.91x** |
| **Avg Latency** | 10.00 ms | 735.55 ms |
| **Peak Queue Depth** | 0 | 992 |

### Latency Analysis
- **p50 (Median)**: ~735 ms (estimated based on uniform workload)
- **p99 (Tail)**: ~1490 ms (estimated based on queue fill rate)

*Note: Latency is higher in parallel execution because jobs spend more time waiting in the `PriorityBlockingQueue` as it fills up instantly, but the overall throughput is significantly higher.*

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
