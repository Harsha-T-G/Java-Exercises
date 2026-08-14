package org.example.employeedirectory.service;

import org.example.employeedirectory.domain.Employee;
import org.example.employeedirectory.exception.DuplicateEmployeeException;
import org.example.employeedirectory.exception.EmployeeNotFoundException;
import org.example.employeedirectory.exception.InvalidEmployeeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class EmployeeDirectory {

    private static final Comparator<Employee> BY_NAME_THEN_ID =
            Comparator.comparing(Employee::name).thenComparing(Employee::employeeId);

    private final Map<String, Employee> employees = new HashMap<>();

    public void addEmployee(Employee employee) {
        validate(employee);
        if (employees.containsKey(employee.employeeId())) {
            throw new DuplicateEmployeeException(
                    "Employee ID already exists: " + employee.employeeId());
        }
        employees.put(employee.employeeId(), employee);
    }

    public Employee findById(String employeeId) {
        Employee employee = employees.get(employeeId);
        if (employee == null) {
            throw new EmployeeNotFoundException("Employee not found: " + employeeId);
        }
        return employee;
    }

    public List<Employee> findByDepartment(String department) {
        if (department == null) {
            return Collections.emptyList();
        }

        List<Employee> matches = new ArrayList<>();
        for (Employee employee : employees.values()) {
            if (employee.department().equals(department)) {
                matches.add(employee);
            }
        }
        matches.sort(BY_NAME_THEN_ID);
        return List.copyOf(matches);
    }

    public List<Employee> findBySkill(String skill) {
        if (skill == null) {
            return Collections.emptyList();
        }

        List<Employee> matches = new ArrayList<>();
        for (Employee employee : employees.values()) {
            boolean hasSkill = employee.skills().stream()
                    .anyMatch(employeeSkill -> employeeSkill != null
                            && employeeSkill.equalsIgnoreCase(skill));
            if (hasSkill) {
                matches.add(employee);
            }
        }
        return List.copyOf(matches);
    }

    public Map<String, List<Employee>> groupByDepartment() {
        Map<String, List<Employee>> groups = new HashMap<>();
        for (Employee employee : employees.values()) {
            groups.computeIfAbsent(employee.department(), ignored -> new ArrayList<>())
                    .add(employee);
        }

        Map<String, List<Employee>> protectedGroups = new HashMap<>();
        groups.forEach((department, group) -> {
            group.sort(BY_NAME_THEN_ID);
            protectedGroups.put(department, List.copyOf(group));
        });
        return Map.copyOf(protectedGroups);
    }

    public Set<String> getAllSkills() {
        Set<String> allSkills = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Employee employee : employees.values()) {
            for (String skill : employee.skills()) {
                if (skill != null) {
                    allSkills.add(skill);
                }
            }
        }
        return Collections.unmodifiableSet(allSkills);
    }

    public void removeEmployee(String employeeId) {
        if (employees.remove(employeeId) == null) {
            throw new EmployeeNotFoundException("Employee not found: " + employeeId);
        }
    }

    private static void validate(Employee employee) {
        if (employee == null) {
            throw new InvalidEmployeeException("Employee must not be null");
        }
        if (isBlank(employee.employeeId())) {
            throw new InvalidEmployeeException("Employee ID must not be null or blank");
        }
        if (isBlank(employee.name())) {
            throw new InvalidEmployeeException("Employee name must not be null or blank");
        }
        if (isBlank(employee.department())) {
            throw new InvalidEmployeeException("Employee department must not be null or blank");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
