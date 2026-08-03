# Exercise 1: Employee Data Analysis using Java Streams

## Objective
Create an Employee model and implement various data analysis operations using Java Streams without modifying the original employee list or using loops for the analysis.

## Requirements
- Create Employee model with: ID, name, department, age, salary, active status, joining date, and skills list
- Create sample data for at least 15 employees across at least 4 departments
- Implement 9 specific queries using Java Streams (no for/while/do-while loops for analysis)
- Use BigDecimal for salary values
- Return empty collections or Optional values instead of null
- Write comprehensive unit tests
- Follow agentic engineering practices (AGENTS.md, CLAUDE.md, AI_USAGE.md)

## Implementation Details

### Employee Model
The Employee class includes:
- Employee ID (String/int)
- Name (String)
- Department (String)
- Age (int)
- Salary (BigDecimal)
- Active status (boolean)
- Joining date (LocalDate)
- Skills (List<String>)

### Service Methods Implemented
1. `getActiveEmployeesSortedBySalaryDesc()` - Active employees sorted by salary (descending)
2. `getEmployeesJoinedLastTwoYears()` - Employees hired in last 2 years
3. `getUniqueSkillsSorted()` - Unique, alphabetically sorted skills
4. `getEmployeesGroupedByDepartment()` - Employees grouped by department
5. `getAverageSalaryByDepartment()` - Average salary per department
6. `getHighestPaidEmployeeByDepartment()` - Highest earner in each department
7. `getTopPaidActiveEmployees(int limit)` - Top N highest-paid active employees
8. `getActiveInactiveCount()` - Count of active vs inactive employees
9. `getEmployeesGroupedBySkill()` - Employee names grouped by skill

## Technical Stack
- Java 17 or 21
- Maven
- JUnit 5
- Java Streams API
- BigDecimal for monetary calculations

## Running the Application
```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Package
mvn package
```

## Agentic Engineering Practices
- AGENTS.md: Project structure, build/test commands, coding standards
- CLAUDE.md: Guidelines for how Claude Code should approach planning, implementation, testing, and review
- AI_USAGE.md: Log of prompts used, suggestions accepted/rejected, and verification methods
- Regular commits with meaningful messages
- Review of all AI-generated code before acceptance