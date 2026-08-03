package org.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.example.model.Employee;
import org.example.sevice.EmployeeDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeDataServiceTest {

    private EmployeeDataService service;
    private List<Employee> employees;

    @BeforeEach
    void setUp() {
        employees = SampleData.createSampleEmployees();
        service = new EmployeeDataService(employees);
    }

    @Test
    void givenEmployees_whenGetActiveEmployeesSortedBySalaryDesc_thenReturnActiveEmployeesSortedDescendingBySalary() {
        // Arrange
        // (Employees and service are set up in @BeforeEach)

        // Act
        List<Employee> activeEmployees = service.getActiveEmployeesSortedBySalaryDesc();

        // Assert
        assertFalse(activeEmployees.isEmpty(), "Should have active employees");

        // Check that all are active
        assertTrue(activeEmployees.stream().allMatch(Employee::isActive),
                "All returned employees should be active");

        // Check sorted descending by salary
        for (int i = 0; i < activeEmployees.size() - 1; i++) {
            assertTrue(activeEmployees.get(i).getSalary()
                    .compareTo(activeEmployees.get(i + 1).getSalary()) >= 0,
                    "Salaries should be in descending order");
        }
    }

    @Test
    void givenEmployees_whenGetEmployeesJoinedLastTwoYears_thenReturnOnlyRecentlyHiredEmployees() {
        // Arrange
        LocalDate twoYearsAgo = LocalDate.now().minusYears(2);

        // Act
        List<Employee> recent = service.getEmployeesJoinedLastTwoYears();

        // Assert
        assertTrue(recent.stream().allMatch(e -> e.getJoiningDate()
                .isAfter(twoYearsAgo) || e.getJoiningDate().isEqual(twoYearsAgo)),
                "All employees should have joined within the last 2 years");
    }

    @Test
    void givenEmployees_whenGetUniqueSkillsSorted_thenReturnDistinctSkillsSortedAlphabetically() {
        // Arrange
        // (Employees and service are set up in @BeforeEach)

        // Act
        List<String> skills = service.getUniqueSkillsSorted();

        // Assert
        assertFalse(skills.isEmpty(), "Should have skills");

        // Check sorted (case-insensitive)
        for (int i = 0; i < skills.size() - 1; i++) {
            assertTrue(skills.get(i).compareToIgnoreCase(skills.get(i + 1)) <= 0,
                    "Skills should be sorted alphabetically");
        }

        // Check no duplicates
        assertEquals(skills.size(), skills.stream().distinct().count(),
                "Skills list should have no duplicates");
    }

    @Test
    void givenEmployees_whenGetEmployeesGroupedByDepartment_thenReturnEmployeesGroupedByDepartmentWithUnmodifiableLists() {
        // Arrange
        // (Employees and service are set up in @BeforeEach)

        // Act
        Map<String, List<Employee>> deptMap = service.getEmployeesGroupedByDepartment();

        // Assert
        assertFalse(deptMap.isEmpty(), "Should have departments");

        // Check that each department's list is unmodifiable
        for (List<Employee> list : deptMap.values()) {
            assertThrowsUnmodifiable(list);
        }

        // Verify that all employees are accounted for
        long totalInMap = deptMap.values().stream().mapToLong(List::size).sum();
        assertEquals(employees.size(), totalInMap,
                "All employees should be present in the grouped map");
    }

    @Test
    void givenEmployees_whenGetAverageSalaryByDepartment_thenReturnAverageSalaryPerDepartmentWithTwoDecimalPlaces() {
        // Arrange
        // (Employees and service are set up in @BeforeEach)

        // Act
        Map<String, BigDecimal> avgMap = service.getAverageSalaryByDepartment();

        // Assert
        assertFalse(avgMap.isEmpty(), "Should have average salaries");
        for (String dept : avgMap.keySet()) {
            BigDecimal avg = avgMap.get(dept);
            assertNotNull(avg, "Average should not be null for department " + dept);
            assertTrue(avg.compareTo(BigDecimal.ZERO) >= 0,
                    "Average salary should be non-negative");
        }
    }

    @Test
    void givenEmployees_whenGetHighestPaidEmployeeByDepartment_thenReturnHighestPaidEmployeePerDepartment() {
        // Arrange
        // (Employees and service are set up in @BeforeEach)

        // Act
        Map<String, Optional<Employee>> highestMap = service.getHighestPaidEmployeeByDepartment();

        // Assert
        assertFalse(highestMap.isEmpty(), "Should have highest paid employees");
        for (String dept : highestMap.keySet()) {
            Optional<Employee> opt = highestMap.get(dept);
            assertTrue(opt.isPresent(), "Should have a highest paid employee for department " + dept);
            Employee emp = opt.get();
            // Verify that this employee has the highest salary in the department
            long countHigher = employees.stream()
                    .filter(e -> e.getDepartment().equals(dept))
                    .filter(e -> e.getSalary().compareTo(emp.getSalary()) > 0)
                    .count();
            assertEquals(0, countHigher,
                    "Employee should have the highest salary in department " + dept);
        }
    }

    @Test
    void givenEmployees_whenGetTopPaidActiveEmployeesWithPositiveLimit_thenReturnTopPaidActiveEmployeesSortedDescending() {
        // Arrange
        int limit = 3;

        // Act
        List<Employee> top = service.getTopPaidActiveEmployees(limit);

        // Assert
        assertEquals(limit, top.size(), "Should return exactly limit employees");

        // Check all are active
        assertTrue(top.stream().allMatch(Employee::isActive),
                "All returned employees should be active");

        // Check sorted descending by salary
        for (int i = 0; i < top.size() - 1; i++) {
            assertTrue(top.get(i).getSalary()
                    .compareTo(top.get(i + 1).getSalary()) >= 0,
                    "Salaries should be in descending order");
        }

        // Edge case: limit <= 0
        assertTrue(service.getTopPaidActiveEmployees(0).isEmpty(),
                "Limit 0 should return empty list");
        assertTrue(service.getTopPaidActiveEmployees(-1).isEmpty(),
                "Negative limit should return empty list");
    }

    @Test
    void givenEmployees_whenGetActiveInactiveCount_thenReturnCorrectCounts() {
        // Arrange
        // (Employees and service are set up in @BeforeEach)

        // Act
        Map<String, Long> counts = service.getActiveInactiveCount();

        // Assert
        assertEquals(2, counts.size(), "Should have active and inactive keys");
        assertTrue(counts.containsKey("active"), "Should have 'active' key");
        assertTrue(counts.containsKey("inactive"), "Should have 'inactive' key");
        long activeCount = employees.stream().filter(Employee::isActive).count();
        long inactiveCount = employees.stream().filter(e -> !e.isActive()).count();
        assertEquals(activeCount, counts.get("active"),
                "Active count should match");
        assertEquals(inactiveCount, counts.get("inactive"),
                "Inactive count should match");
    }

    @Test
    void givenEmployees_whenGetEmployeesGroupedBySkill_thenReturnEmployeesGroupedBySkillWithUnmodifiableLists() {
        // Arrange
        // (Employees and service are set up in @BeforeEach)

        // Act
        Map<String, List<String>> skillMap = service.getEmployeesGroupedBySkill();

        // Assert
        assertFalse(skillMap.isEmpty(), "Should have skills mapped to employee names");
        // Check that each list of names is unmodifiable
        for (List<String> names : skillMap.values()) {
            assertThrowsUnmodifiable(names);
        }
        // Verify that each employee appears for each of their skills
        for (Employee emp : employees) {
            for (String skill : emp.getSkills()) {
                assertTrue(skillMap.containsKey(skill),
                        "Skill " + skill + " should be in the map");
                assertTrue(skillMap.get(skill).contains(emp.getName()),
                        "Employee " + emp.getName() + " should be listed under skill " + skill);
            }
        }
    }

    private <T> void assertThrowsUnmodifiable(List<T> list) {
        UnsupportedOperationException ex = assertThrows(
                UnsupportedOperationException.class,
                () -> {
                    if (list.isEmpty()) {
                        list.add(null);
                    } else {
                        list.add(list.get(0));
                    }
                },
                "List should be unmodifiable");
        assertNotNull(ex);
    }
}