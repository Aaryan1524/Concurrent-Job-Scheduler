# Concurrent Job Scheduler: Learning Log

## Sprint 0: Foundation

(See previous logs for initial structure)

## Sprint 1: Execution Engine

### ThreadPoolExecutor Configuration
A `ThreadPoolExecutor` is the "engine room" of modern Java concurrency. It manages a pool of worker threads to execute tasks.

#### corePoolSize vs maximumPoolSize
- **corePoolSize**: The minimum number of threads kept alive in the pool, even if they are idle. This is like your base staff—always ready.
- **maximumPoolSize**: The absolute ceiling on how many threads the pool can spawn. If your tasks overflow your internal queue, the pool will hire "temporary help" up to this limit.
- **Why it matters**: A high `corePoolSize` consumes resources but reacts instantly. A low one saves memory but might cause delays under load.

### Callable vs Runnable
We switched our `Job` tasks from `Runnable` to `Callable`.
- **Runnable**: "Fire and forget." No return value, no checked exceptions. It's like sending a letter with no return address.
- **Callable**: "Submit and wait." It returns a value and can throw exceptions. It's like making a phone call—you expect a response (or a hang-up).
- **CompletableFuture**: We used this to bridge the gap. It's a container for a result that hasn't happened yet. It allows us to handle results asynchronously as they arrive.

### Custom Worker & Priority Pattern
The standard `ThreadPoolExecutor` doesn't easily support dynamic priority changes once tasks are in its internal queue. By using a custom `JobWorker` and a separate `PriorityBlockingQueue`:
1. **Prioritization**: When a thread becomes free, it goes to our `PriorityBlockingQueue` and says "Give me the most urgent job available right now."
2. **Decoupling**: The executor handles thread management (hiring/firing), while we handle the sorting (prioritization). This separation of concerns is a professional design pattern.

---
*Next: Handling errors and retries.*
