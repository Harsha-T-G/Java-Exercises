# CLAUDE.md - Claude Code Usage Guidelines for Exercise 4

## How Claude Code Should Approach Exercise 4: Identify and Fix a Race Condition

### Planning Phase
1. **Problem Understanding**:
   - Clearly grasp the race condition scenario: multiple threads updating shared inventory count
   - Identify the critical section: stock decrement operation
   - Recognize the consequences: negative inventory, incorrect final count
   - Understand requirements: initial stock of 1,000 units, at least 100 concurrent tasks

2. **Solution Approach Planning**:
   - Plan to create three versions: unsynchronized, synchronized, and using modern concurrency utilities
   - Determine comparison criteria: correctness, performance, readability
   - Plan comprehensive test strategy to detect race conditions
   - Decide on measurements: execution time, correctness verification

3. **Specification Creation**:
   - Define InventoryItem class with thread-safe/unsafe variants
   - Plan test scenarios that will reliably expose race conditions
   - Define metrics for comparing different synchronization approaches
   - Outline requirements for graceful shutdown and resource cleanup

### Agentic Engineering Practices to Follow
1. **Toolchain Setup**: Properly configured AGENTS.md and CLAUDE.md files
2. **Spec Framing**: Clear understanding of race condition problem and solution requirements before coding
3. **Evidence-led Development**: TDD approach with tests that can expose race conditions
4. **Context Management**: Relevant context maintained in CLAUDE.md for Claude Code
5. **Workflows**: Standardized process for identifying, implementing, and verifying thread-safe solutions
6. **Skill Packaging**: Reusable components for common concurrency patterns
7. **Code Review**: Special focus on thread safety verification
8. **Token Economics**: Efficient AI usage with clear, specific prompts about concurrency concerns
9. **Refactoring**: Improving code structure while maintaining thread safety
10. **Retrospective**: Analyzing which concurrency approach worked best and why

### Implementation Phase
1. **Base Implementation** (Unsafe Version):
   - Create simple InventoryItem class with non-atomic stock decrement
   - Implement basic task that repeatedly reduces stock
   - Create test that runs multiple threads and expects incorrect final result
   - This version should clearly demonstrate the race condition

2. **Thread-Safe Versions**:
   For each synchronization approach (synchronized, Lock, AtomicInteger):
   - Implement InventoryItem with the specific synchronization mechanism
   - Ensure all stock access/modification is properly protected
   - Maintain same public interface for interchangeability
   - Document thread safety guarantees in Javadoc

3. **Test Harness Development**:
   - Create test framework that can run same test logic with different implementations
   - Implement ability to run 100+ concurrent tasks
   - Add verification that stock never goes negative
   - Add verification that final stock is correct (initial - total consumed)
   - Implement proper executor service shutdown
   - Add timing measurements for performance comparison

4. **Specific Implementation Details**:
   - **Synchronized Approach**: Use synchronized methods or blocks on intrinsic lock
     * Simple but can cause contention under high load
     * Good for low to moderate contention scenarios
   - **Lock Approach**: Use java.util.concurrent.locks.ReentrantLock
     * More flexible (tryLock, lockInterruptible, timeout)
     * Similar performance characteristics to synchronized in uncontended case
   - **AtomicInteger Approach**: Use java.util.concurrent.atomic.AtomicInteger
     * Best performance for simple increment/decrement operations
     * Lock-free using hardware CAS instructions
     * Most suitable for this specific use case (just a counter)

5. **Key Implementation Considerations**:
   - Ensure visibility of changes across threads (proper synchronization or volatile)
   - Avoid holding locks during expensive operations
   - Consider fairness vs. performance tradeoffs
   - Handle potential integer overflow (though unlikely with reasonable bounds)
   - Ensure proper exception handling doesn't leave locks locked

### Testing Phase
1. **Race Condition Detection**:
   - Create tests that run many threads performing rapid operations
   - Use assertEquals to verify final count matches expected value
   - Run tests multiple times (race conditions are probabilistic)
   - For unsafe version, expect failures; for safe versions, expect consistent success

2. **Safety Verification**:
   - Add assertions that stock never goes below zero during execution
   - Use atomic variables or thread-safe collections to track minimum observed value
   - Validate that all threads complete their work (no lost updates)

3. **Performance Comparison**:
   - Measure execution time for each approach under same workload
   - Record results to inform recommendation in final report
   - Consider testing with different contention levels (few threads vs many threads)

4. **Specific Test Requirements**:
   - Test with at least 100 concurrent tasks as specified
   - Each task should attempt multiple stock reductions
   - Verify proper executor service shutdown (no resource leaks)
   - Test interruption handling if applicable
   - Verify no lost updates when using thread-safe versions

### Review Phase
1. **Thread Safety Verification Checklist**:
   - [ ] All shared state access properly synchronized or using atomic variables
   - [ ] No race conditions in stock decrement/checked operations
   - [ ] Visibility guarantees established (changes visible to all threads)
   - [ ] No deadlock possibilities
   - [ ] No resource leaks (executors properly shutdown)
   - [ ] Consistent behavior under high concurrency
   - [ ] Stock never negative at any point during execution
   - [ ] Final stock equals initial stock minus total consumed amount

2. **Implementation Quality Checklist**:
   - [ ] Three distinct versions implemented: unsynchronized, synchronized, Lock-based, AtomicInteger-based
   - [ ] Clear documentation explaining approach and trade-offs for each
   - [ ] Consistent public interface across all implementations
   - [ ] Meaningful, descriptive names for classes, methods, variables
   - [ ] Proper exception handling and resource cleanup
   - [ ] Comprehensive test suite covering requirements
   - [ ] Clear comparison and recommendation in documentation
   - [ ] All tests pass consistently

3. **Concurrency-Specific Checks**:
   - [ ] No busy-waiting loops
   - [ ] Proper use of concurrent utilities where appropriate
   - [ ] Minimal time spent in synchronized blocks/locks
   - [ ] Correct use of volatile where needed for visibility
   - [ ] Avoidance of Thread.sleep() in synchronization logic (except for deliberate delays in testing)

4. **Documentation Requirements**:
   - [ ] Clear explanation of the race condition problem
   - [ ] Description of each solution approach
   - [ ] Comparison of approaches (correctness, performance, complexity)
   - [ ] Justification for selected approach
   - [ ] Instructions for running and verifying thread safety tests
   - [ ] Lessons learned about concurrency in Java

### Claude Code Specific Practices
- Carefully review all concurrency-related code for thread safety issues
- Pay special attention to shared state access patterns
- Verify that locking is used correctly and consistently
- Check for proper visibility guarantees
- Ensure test cases are designed to actually trigger race conditions when present
- Validate that performance measurements are conducted fairly
- Document reasoning clearly when comparing different approaches
- Ensure all generated code follows Java concurrency best practices