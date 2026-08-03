# CLAUDE.md - Claude Code Usage Guidelines for Exercise 2

## How Claude Code Should Approach Exercise 2: Order and Revenue Report

### Planning Phase
1. **Requirement Analysis**:
   - Carefully read all 9 report requirements from the exercise description
   - Identify Order model attributes: ID, customer ID, category, order amount, status (CREATED, COMPLETED, CANCELLED), date
   - Note constraints: BigDecimal for amounts, exclude non-COMPLETED orders from revenue calculations, use Streams and collectors only
   - Identify required report types: total revenue, revenue by category, revenue by customer, highest-value customer, status grouping, partition by value, highest revenue category, monthly summary, most recent orders

2. **Specification Creation**:
   - Define clear input/output for each of the 9 required report methods
   - Plan comprehensive test data including: empty orders, no completed orders, equal totals, multiple months, ties for highest values
   - Determine appropriate return types for each method (BigDecimal, Maps, Lists, Optionals, etc.)
   - Plan sample data generation: at least 20 orders with varied attributes

3. **Architecture Planning**:
   - Model: Order class with LocalDate for date, BigDecimal for amount, enums or constants for status/category if appropriate
   - Service: OrderReportService with 9 methods matching requirements
   - Utility: Potential helper methods for filtering completed orders (used by multiple reports)
   - Test: Comprehensive JUnit 5 test suite covering all requirements and edge cases

### Implementation Phase
1. **Model First**: Create Order class with:
   - Proper validation (non-null required fields where applicable)
   - Getters for all fields
   - Proper equals and hashCode methods
   - Consider using enum for OrderStatus if beneficial

2. **Service Implementation Strategy**:
   - Start with filtering since multiple reports operate on completed orders only
   - Create private helper method: `getCompletedOrders()` returning filtered stream
   - Implement one report method at a time using Test-Driven Development
   - Key stream operations and collectors to leverage:
     * filter() - for isolating completed operations
     * map() - for extracting fields (amount, customerId, etc.)
     * Collectors.summingDouble()/summingLong() - adapted for BigDecimal sums
     * Collectors.averagingDouble() - for average calculations if needed
     * Collectors.groupingBy() - for grouping by category, customer, status, month
     * Collectors.mapping() - for transforming values within collectors
     * Collectors.collectingAndThen() - for creating immutable results
     * Collectors.maxBy()/minBy() - for finding extremes (highest revenue, etc.)
     * Collectors.partitioningBy() - for separating high-value vs regular orders
     * Collectors.toList() - for collecting results to lists
     * Collectors.joining() - for string concatenation if needed
   - BigDecimal handling:
     * For sums: use reducing() collector with BigDecimal::add and BigDecimal.ZERO
     * For averages: sum then divide by count with appropriate rounding
     * For comparisons: use compareTo() method
   - Date handling for monthly reports:
     * Extract year-month using DateTimeFormatter or ChronoUnit
     * Group by year-month string or YearMonth object
   - Immutability: Return unmodifiable collections where appropriate

3. **Specific Method Implementation Guidance**:
   - `calculateTotalRevenue()`: Stream of completed orders, map to amount, sum with BigDecimal
   - `calculateRevenueByCategory()`: Group completed orders by category, sum amounts per group
   - `calculateRevenueByCustomer()`: Group completed orders by customer ID, sum amounts per group
   - `findCustomerWithHighestValue()`: Use maxBy on customer revenue map entry
   - `groupOrdersByStatus()`: Group all orders by status enum/string
   - `partitionCompletedOrders()`: Partition completed orders by amount >= 10000
   - `findCategoryWithHighestRevenue()`: Similar to highest customer but for categories
   - `monthlyRevenueSummary()`: Group by month (year-month), sum amounts, sort by month
   - `findFiveMostRecentCompletedOrders()`: Sort completed orders by date descending, limit 5

### Testing Phase
1. **Test Data Creation**:
   - Normal dataset: 20+ orders with varied customers, categories, amounts, dates, statuses
   - Edge case 1: Empty order list
   - Edge case 2: No completed orders (only CREATED/CANCELLED)
   - Edge case 3: Equal totals for ties testing
   - Edge case 4: Multiple months for chronological ordering
   - Edge case 5: Boundary values for high-value threshold (exactly 10000, 9999.99, 10000.01)
   - Edge case 6: Orders with same dates for most recent sorting

2. **Comprehensive Test Coverage**:
   - Each of the 9 methods tested with normal data
   - Each method tested with empty input (return appropriate empty result)
   - Each method tested with no completed orders (where applicable)
   - Specific tests for boundary conditions and edge cases mentioned
   - Verification that BigDecimal precision is maintained
   - Confirmation that no loops are used in implementation (code inspection)
   - Validation that returned collections are truly immutable where expected
   - Tests for correct handling of DATE boundaries (month/year transitions)

3. **Special Test Cases to Verify**:
   - Monthly report correctly orders by chronological month (not alphabetically)
   - High-value partition correctly identifies 10000+ as high value
   - Most recent orders correctly sorted by date descending
   - Tie-breaking behavior where applicable (which item returned when equal?)
   - Proper handling of null/empty values where relevant

### Review Phase
1. **Pre-Commit Checklist**:
   - [ ] Zero for/while/do-while loops in report calculation methods
   - [ ] All monetary calculations use BigDecimal exclusively
   - [ ] Only COMPLETED orders included in revenue calculations
   - [ ] Proper handling of empty inputs (return empty collections/zero values)
   - [ ] All 9 required report methods implemented per specifications
   - [ ] Meaningful, descriptive method and variable names
   - [ ] Methods appropriately focused and sized (SRP)
   - [ ] Comprehensive test suite covering requirements and edge cases
   - [ ] Clean compilation with no warnings
   - [ ] All tests pass including edge case tests
   - [ ] No unused imports or dead code
   - [ ] Proper Javadoc documentation for public methods
   - [ ] Meaningful Git commits with descriptive messages explaining what and why

2. **Claude Code Specific Practices**:
   - Thorough review of every line of AI-generated code before acceptance
   - Reference CLAUDE.md throughout implementation for guidance
   - Maintain AI_USAGE.md with detailed prompts, accepted/rejected suggestions, and verification
   - If AI output doesn't meet requirements, refine prompt with more specifics and regenerate
   - Execute comprehensive test suite after each AI-generated code implementation
   - Use iterative approach: implement one method, test thoroughly, then proceed to next

---

## Agentic Engineering Practices (from AGENTS.md)

### Project Structure
```
Exercise2_OrderRevenueReport/
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── model/          # Order model
│   │       └── service/        # OrderReportService implementations
│   └── test/
│       └── java/
│           └── service/        # Test classes
├── .claude/                    # Claude-specific configurations
│   ├── agents/                 # Custom agent definitions
│   ├── workflows/              # Workflow definitions
│   └── settings.json           # Claude Code settings
├── .agents/                    # Agent definitions and configurations
│   ├── planner/                # Planning agents
│   ├── reviewer/               # Code review agents
│   └── tester/                 # Testing agents
├── pom.xml                     # Maven configuration
├── README.md                   # Project overview
├── AGENTS.md                   # This file
├── CLAUDE.md                   # Claude Code usage guidelines
└── AI_USAGE.md                 # AI usage tracking

### Agentic Engineering Practices to Follow
1. **Toolchain Setup**: Properly configured AGENTS.md and CLAUDE.md files
2. **Spec Framing**: Clear understanding of all 9 report requirements before coding
3. **Evidence-led Development**: Test-driven development with comprehensive test cases
4. **Context Management**: Relevant context maintained in CLAUDE.md for Claude Code
5. **Workflows**: Standardized development workflow followed consistently
6. **Skill Packaging**: Reusable components for common order processing operations
7. **Code Review**: Automated and manual review against requirements
8. **Token Economics**: Efficient AI usage with clear, specific prompts
9. **Refactoring**: Continuous improvement of code structure and readability
10. **Retrospective**: Regular process improvement based on what worked/didn't work

### Build and Test Commands
- **Compile**: `mvn clean compile`
- **Test**: `mvn test`
- **Package**: `mvn package`
- **Verify**: `mvn verify`
- **Run specific test**: `mvn test -Dtest=OrderReportServiceTest`

### Coding Standards
- Java 17 or 21
- Meaningful, descriptive names for classes, methods, variables
- Small, focused methods following Single Responsibility Principle
- Comprehensive input validation and important edge case handling
- No exposure of mutable internal collections (return unmodifiable views)
- BigDecimal for all monetary calculations (order amounts)
- Java Streams and collectors for all report calculations (no for/while loops)
- Return empty collections or appropriate alternatives instead of null
- Meaningful Git commits with descriptive messages
- Thorough review of all AI-generated code before acceptance