# CLAUDE.md - Claude Code Usage Guidelines for Exercise 3

## Project Structure
```
Exercise3_FunctionalDiscountPipeline/
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── model/          # Order model
│   │       ├── pipeline/       # Validation and discount pipeline components
│   │       └── service/        # Order processing service
│   └── test/
│       └── java/
│           ├── pipeline/       # Pipeline component tests
│           └── service/        # Service tests
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
├── AGENTS.md                   # Agentic Engineering Guidelines (see CLAUDE.md for details)
├── CLAUDE.md                   # This file
└── AI_USAGE.md                 # AI usage tracking
```

## How Claude Code Should Approach Exercise 3: Functional Discount and Validation Pipeline

### Planning Phase
1. **Requirement Analysis**:
   - Carefully read all requirements for the functional discount and validation pipeline
   - Identify Order model attributes: ID, customer type (REGULAR, PREMIUM, CORPORATE), amount, item count, coupon code (optional)
   - Note constraints: No large switch/if-else chains, use Predicate for validation, Function for discount calculation, Optional for coupon, immutable result
   - Identify required components: validation predicates, discount functions, pipeline composition, result object

2. **Specification Creation**:
   - Define validation predicates: ID present, amount > 0, item count > 0
   - Define discount functions: different calculations for each customer type
   - Plan coupon handling: Optional additional discount that can be applied after customer discount
   - Design result object: immutable container for original amount, discount amount, final amount
   - Plan function composition approach: how to combine validations and discounts

3. **Architecture Planning**:
   - Model: Order class with appropriate fields and customer type enum
   - Validation: Predicate<Order> implementations for each rule
   - Discount: Function<Order, BigDecimal> implementations for each customer type
   - Pipeline: Method to compose validations and apply discounts
   - Result: Immutable class holding calculation results
   - Service: Main interface for processing orders through the pipeline
   - Test: Comprehensive JUnit 5 test suite

### Implementation Phase
1. **Model First**: Create Order class with:
   - Proper validation in constructor (non-null ID, positive amounts where required)
   - Getters for all fields
   - CustomerType enum (REGULAR, PREMIUM, CORPORATE)
   - Proper equals and hashCode methods

2. **Validation Components**:
   - Create Predicate<Order> implementations for each validation rule:
     * IdNotNullPredicate
     * PositiveAmountPredicate
     * PositiveItemCountPredicate
   - Consider creating a ValidationUtils class with methods to combine predicates (AND/OR logic)
   - All validation should be pure functions with no side effects

3. **Discount Components**:
   - Create Function<Order, BigDecimal> implementations for each customer type:
     * RegularCustomerDiscount
     * PremiumCustomerDiscount
     * CorporateCustomerDiscount
   - Each should calculate discount based on order properties (amount, item count, etc.)
   - Consider making discount rates configurable rather than hard-coded
   - All discount functions should be pure (same input always produces same output)

4. **Coupon Handling**:
   - Use Optional<BigDecimal> or Optional<DiscountFunction> for coupon
   - Create method to apply coupon discount after customer discount
   - Ensure coupon can be absent (Optional.empty())

5. **Pipeline Composition**:
   - Create method that validates order using all required predicates
   - If validation passes, calculate base discount using customer type function
   - Apply coupon discount if present
   - Ensure final amount is never negative (max(0, calculated amount))
   - Return immutable result object

6. **Result Object**:
   - Create immutable class with:
     * originalAmount: BigDecimal
     * discountAmount: BigDecimal
     * finalAmount: BigDecimal (guaranteed >= 0)
   - Provide getters but no setters
   - Proper equals, hashCode, toString

### Testing Phase
1. **Test Data Creation**:
   - Valid orders for each customer type with various amounts and item counts
   - Invalid orders: null ID, zero/negative amount, zero/negative item count
   - Orders with and without coupons
   - Edge case amounts that could lead to negative final amounts

2. **Component Testing**:
   - Test each Predicate in isolation with appropriate test data
   - Test each Function (discount calculator) in isolation
   - Test coupon application logic
   - Test validation combination (AND/OR logic if implemented)

3. **Pipeline Testing**:
   - Test valid orders produce correct results
   - Test invalid orders are properly rejected (however your design handles this)
   - Test coupon application works correctly
   - Test that final amount is never negative
   - Test all customer types with various scenarios
   - Test edge cases like zero discounts, 100% discounts, etc.

4. **Functional Programming Principles**:
   - Verify no side effects in validation or discount functions
   - Confirm referential transparency (same inputs always produce same outputs)
   - Check that functions don't modify input Order objects
   - Validate proper use of method references where appropriate
   - Confirm avoidance of large switch/if-else chains for customer types

5. **Test Writing Best Practices**:
   - Use Arrange-Act-Assert (AAA) pattern for test organization
   - Use Given-When-Then (GWT) format for test method names: `given_when_then_expectedResult()`
   - Example: `givenRegularCustomer_withAmount100_andCoupon10Percent_whenCalculatingDiscount_thenFinalAmountIs80()`

### Review Phase
1. **Pre-Commit Checklist**:
   - [ ] No large switch or if/else chains for customer discount calculation
   - [ ] Validation uses Predicate<Order> interfaces
   - [ ] Discount calculation uses Function<Order, BigDecimal> interfaces
   - [ ] Coupon code handled with Optional
   - [ ] Result is immutable object with original, discount, and final amounts
   - [ ] Final amount never negative
   - [ ] Adding new customer/discount rule requires minimal changes
   - [ ] Meaningful, descriptive names for all classes, methods, variables
   - [ ] Methods appropriately focused and sized (single responsibility)
   - [ ] Comprehensive test suite covering all requirements and edge cases
   - [ ] Clean compilation with no warnings
   - [ ] All tests pass including edge case tests
   - [ ] No unused imports or dead code
   - [ ] Proper JavaDoc documentation for public methods and interfaces
   - [ ] Meaningful Git commits with descriptive messages

2. **Claude Code Specific Practices**:
   - Examine every line of AI-generated code before acceptance
   - Refer to CLAUDE.md throughout implementation for guidance on functional approach
   - Maintain AI_USAGE.md with detailed records of prompts, accepted/rejected suggestions, and verification methods
   - If AI output violates functional requirements, provide specific feedback and regenerate
   - Execute full test suite after each AI-generated code implementation
   - Use iterative approach: implement one component, test thoroughly, proceed to next
   - Pay special attention to ensuring no mutation of input objects
   - Verify proper use of Java 8+ functional interfaces and lambda expressions/method references

## Agentic Engineering Practices to Follow
1. **Toolchain Setup**: Configure AGENTS.md and CLAUDE.md as guardians of development practices
2. **Spec Framing**: Clearly define validation rules and discount pipelines before implementation
3. **Evidence-led Development**: Test-driven development for each validation rule and discount calculation
4. **Context Management**: Keep functional programming principles and requirements in CLAUDE.md
5. **Workflows**: Follow established workflow for building functional pipelines (define -> test -> implement)
6. **Skill Packaging**: Create reusable validation and discount functions that can be composed
7. **Code Review**: Verify functional purity, no side effects, and proper use of interfaces
8. **Token Economics**: Use AI effectively for generating functional interfaces and lambda expressions
9. **Refactoring**: Continuously improve function composition and method reference usage
10. **Retrospective**: Regularly assess if functional approach is yielding cleaner, more maintainable code

## Build and Test Commands
- **Compile**: `mvn clean compile`
- **Test**: `mvn test`
- **Package**: `mvn package`
- **Verify**: `mvn verify`
- **Run specific test**: `mvn test -Dtest=DiscountPipelineTest`

## Coding Standards
- Java 17 or 21
- Meaningful, descriptive names for classes, methods, variables, and functions
- Small, focused functions following Single Responsibility Principle
- Predicate, Function, and other functional interfaces for validation and transformation
- Proper use of Optional for potentially absent values (coupon code)
- Immutable result objects containing original amount, discount amount, final amount
- No one large switch/if-else chains for discount calculation (use function composition)
- Minimal changes required when adding new customer types or discount rules
- Final amount must never be negative
- Meaningful Git commits
- Comprehensive review of all AI-generated functional code