package org.example.employeedirectory.service;

import org.example.employeedirectory.domain.Employee;
import org.example.employeedirectory.exception.DuplicateEmployeeException;
import org.example.employeedirectory.exception.EmployeeNotFoundException;
import org.example.employeedirectory.exception.InvalidEmployeeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmployeeDirectoryTest {

    private EmployeeDirectory directory;

    @BeforeEach
    void setUp() {
        directory = new EmployeeDirectory();
    }

    @Test
    void givenValidEmployee_whenAddedAndFoundById_thenReturnsEmployee() {
        Employee employee = employee("E-1", "Asha", "Engineering", "Java");

        directory.addEmployee(employee);

        assertEquals(employee, directory.findById("E-1"));
    }

    @Test
    void givenExistingEmployeeId_whenAddedAgain_thenRejectsDuplicate() {
        directory.addEmployee(employee("E-1", "Asha", "Engineering", "Java"));

        assertThrows(DuplicateEmployeeException.class,
                () -> directory.addEmployee(employee("E-1", "Ben", "Finance", "Excel")));
    }

    @Test
    void givenInvalidRequiredData_whenEmployeeIsAdded_thenRejectsIt() {
        assertThrows(InvalidEmployeeException.class, () -> directory.addEmployee(null));
        assertThrows(InvalidEmployeeException.class,
                () -> directory.addEmployee(employee(" ", "Asha", "Engineering")));
        assertThrows(InvalidEmployeeException.class,
                () -> directory.addEmployee(employee("E-1", null, "Engineering")));
        assertThrows(InvalidEmployeeException.class,
                () -> directory.addEmployee(employee("E-1", "Asha", " ")));
    }

    @Test
    void givenEmployeesInDepartment_whenFoundByDepartment_thenSortsByNameAndId() {
        Employee zoe = employee("E-3", "Zoe", "Engineering", "Java");
        Employee ashaTwo = employee("E-2", "Asha", "Engineering", "SQL");
        Employee ashaOne = employee("E-1", "Asha", "Engineering", "Python");
        directory.addEmployee(zoe);
        directory.addEmployee(ashaTwo);
        directory.addEmployee(ashaOne);
        directory.addEmployee(employee("E-4", "Ben", "Finance", "Excel"));

        List<Employee> result = directory.findByDepartment("Engineering");

        assertEquals(List.of(ashaOne, ashaTwo, zoe), result);
    }

    @Test
    void givenEmployeesWithSkill_whenSearchedUsingDifferentCase_thenReturnsMatches() {
        Employee asha = employee("E-1", "Asha", "Engineering", "Java", "SQL");
        Employee ben = employee("E-2", "Ben", "Finance", "Excel");
        directory.addEmployee(asha);
        directory.addEmployee(ben);

        assertEquals(List.of(asha), directory.findBySkill("jAvA"));
        assertEquals(List.of(), directory.findBySkill("Kotlin"));
    }

    @Test
    void givenRepeatedSkillsWithDifferentCase_whenAllSkillsRequested_thenReturnsUniqueSortedSet() {
        directory.addEmployee(employee("E-1", "Asha", "Engineering", "SQL", "Java"));
        directory.addEmployee(employee("E-2", "Ben", "Finance", "java", "Python"));

        Set<String> skills = directory.getAllSkills();

        assertEquals(List.of("Java", "Python", "SQL"), new ArrayList<>(skills));
    }

    @Test
    void givenExistingEmployee_whenRemoved_thenItCanNoLongerBeFound() {
        directory.addEmployee(employee("E-1", "Asha", "Engineering", "Java"));

        directory.removeEmployee("E-1");

        assertThrows(EmployeeNotFoundException.class, () -> directory.findById("E-1"));
    }

    @Test
    void givenMissingEmployee_whenRemoved_thenThrowsEmployeeNotFoundException() {
        assertThrows(EmployeeNotFoundException.class, () -> directory.removeEmployee("missing"));
    }

    @Test
    void givenReturnedCollections_whenModificationIsAttempted_thenInternalDataIsProtected() {
        HashSet<String> originalSkills = new HashSet<>(Set.of("Java"));
        Employee asha = new Employee("E-1", "Asha", "Engineering", originalSkills);
        directory.addEmployee(asha);
        originalSkills.add("Python");

        List<Employee> departmentEmployees = directory.findByDepartment("Engineering");
        Map<String, List<Employee>> groups = directory.groupByDepartment();
        Set<String> allSkills = directory.getAllSkills();

        assertEquals(Set.of("Java"), asha.skills());
        assertThrows(UnsupportedOperationException.class, () -> asha.skills().add("SQL"));
        assertThrows(UnsupportedOperationException.class, () -> departmentEmployees.clear());
        assertThrows(UnsupportedOperationException.class, () -> groups.put("Finance", List.of()));
        assertThrows(UnsupportedOperationException.class,
                () -> groups.get("Engineering").clear());
        assertThrows(UnsupportedOperationException.class, () -> allSkills.add("SQL"));
        assertEquals(List.of(asha), directory.findByDepartment("Engineering"));
    }

    @Test
    void givenNullSkills_whenEmployeeIsAdded_thenSkillsAreTreatedAsOptional() {
        Employee employee = new Employee("E-1", "Asha", "Engineering", null);

        directory.addEmployee(employee);

        assertEquals(Set.of(), directory.findById("E-1").skills());
        assertEquals(Set.of(), directory.getAllSkills());
    }

    private static Employee employee(
            String employeeId, String name, String department, String... skills) {
        return new Employee(employeeId, name, department, new HashSet<>(List.of(skills)));
    }
}
