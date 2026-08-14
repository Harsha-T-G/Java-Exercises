# CLAUDE.md - Claude Code Usage Guidelines for Exercise 1

## How Claude Code Should Approach This Exercise

### Planning Phase
1. **Requirement Analysis**:
   - Identify the 9 specific stream operations needed
   - Note constraints: BigDecimal for salary, no loops in analysis, 15+ employees across 4+ departments
   - Identify Employee model attributes: ID, name, department, age, salary, active status, joining date, skills

2. **Specification Creation**:
   - Create clear acceptance criteria for each of the 9 operations
   - Define test cases for normal data, empty input, duplicate skills, single-employee departments
   - Plan sample data generation meeting requirements

3. **Architecture Planning**:
   - Model: Employee class with appropriate fields
   - Service: EmployeeDataService with 9 stream-based methods
   - Test: Comprehensive JUnit 5 test suite

### Implementation Phase
1. **Model First**: Create Employee class with:
   - Proper validation in constructor
   - Getters for all fields
   - Immutable skills list (return defensive copy)
   - Equals and hashCode methods

2. **Service Implementation**:
   - Implement one method at a time using TDD
   - Use Java Streams exclusively for all analysis operations
   - Key stream operations to use:
     * filter() - for selecting active employees, recent joiners
     * map() - for extracting properties (names, skills)
     * sorted() with Comparator - for ordering (especially BigDecimal comparison)
     * distinct() - for unique skills
     * groupingBy() - for department groupings
     * summarizingDouble()/averagingDouble() - for average salary (adapt for BigDecimal)
     * maxBy()/minBy() - for highest paid employees
     * partitioningBy() - for active/inactive split
     * flatMap() - for extracting skills from employees
     * collectingAndThen() - for immutable collections
   - Handle BigDecimal properly for calculations and comparisons
   - Never modify input employee list
   - Return unmodifiable collections or appropriate wrappers (Optional)

3. **BigDecimal Specifics**:
   - For average: sum salaries, divide by count, round to 2 decimal places
   - Use MathContext or setScale(2, RoundingMode.HALF_UP)
   - For comparisons: use compareTo() method

### Testing Phase
1. **Test Data Creation**:
   - Normal dataset: 15+ employees across 4+ departments with varied data
   - Edge cases: empty list, single employee per department, duplicate skills
   - Date boundary cases: employees hired exactly 2 years ago
   - Salary edge cases: zero, same salaries, very large values

2. **Test Each Method**:
   - Verify correct output for normal cases
   - Test edge cases return appropriate values (empty collections, Optionals.empty())
   - Confirm immutability of returned collections (attempt to modify should fail)
   - Validate BigDecimal precision and rounding
   - Ensure no modification of input data streams

3. **Specific Test Cases**:
   - Verify no loops used in analysis methods (can check via code inspection)
   - Test all 9 operations with various data sets
   - Verify performance characteristics of stream operations

### Review Phase
1. **Self-Review Checklist**:
   - [ ] No for/while/do-while loops in any analysis method
   - [ ] BigDecimal used for all salary calculations
   - [ ] Original employee list never modified during processing
   - [ ] Proper handling of empty inputs (return empty collections/Optionals.empty())
   - [ ] All 9 stream operations correctly implemented per exercise specs
   - [ ] Descriptive, meaningful method and variable names
   - [ ] Methods appropriately sized and focused (single responsibility)
   - [ ] Comprehensive unit tests for all methods including edge cases
   - [ ] Code compiles without warnings
   - [ ] All tests pass including edge case tests
   - [ ] No unused imports or dead code
   - [ ] Proper JavaDoc comments for public methods
   - [ ] Meaningful Git commits with descriptive messages

2. **Claude Code Specific**:
   - Review every line of AI-generated code before accepting
   - Use CLAUDE.md as reference during implementation
   - Keep AI_USAGE.md updated with prompts and verification steps
   - If dissatisfied with AI generation, refine prompt and regenerate
   - Always run tests after AI-generated code implementation

### Workflow Recommendation
1. Plan: Update CLAUDE.md with specific approach for next method
2. Implement: Ask Claude Code to implement one method with TDD (write test first)
3. Test: Run tests to verify implementation
4. Review: Check against checklist in this file
5. Commit: Meaningful commit message
6. Repeat: For each of the 9 methods