# CLAUDE.md - Claude Code Usage Guidelines for Exercise 4

Please refer to AGENTS.md for detailed instructions on how Claude Code should approach Exercise 4: Identify and Fix a Race Condition.

AGENTS.md contains comprehensive guidance covering:
- Problem understanding and solution approach planning
- Implementation phases for unsafe and thread-safe versions
- Testing strategies for detecting race conditions
- Performance comparison and verification requirements
- Claude Code specific practices for concurrency-related work

**Selected Approach**: After evaluating synchronized, Lock, and AtomicInteger approaches, we selected the AtomicInteger-based solution for its lock-free efficiency, suitability for simple counter operations, and consistent performance under contention.

See AGENTS.md for complete details.