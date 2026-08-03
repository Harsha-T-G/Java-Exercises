package org.example.sevice;

import org.example.model.Employee;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.maxBy;

public class EmployeeDataService {

    private final List<Employee> employees;

    public EmployeeDataService(List<Employee> employees) {
        this.employees = Objects.requireNonNull(employees);
    }

    public List<Employee> getActiveEmployeesSortedBySalaryDesc() {
        return employees.stream()
                .filter(Employee::isActive)
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .toList();
    }

    public List<Employee> getEmployeesJoinedLastTwoYears() {
        LocalDate twoYearsAgo = LocalDate.now().minusYears(2);

        return employees.stream()
                .filter(e -> e.getJoiningDate().isAfter(twoYearsAgo))
                .toList();
    }


    public List<String> getUniqueSkillsSorted() {
        return employees.stream()
                .flatMap(e -> e.getSkills().stream())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public Map<String, List<Employee>> getEmployeesGroupedByDepartment() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.toUnmodifiableList()
                ));
    }

    public Map<String, BigDecimal> getAverageSalaryByDepartment() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.teeing(
                                Collectors.reducing(BigDecimal.ZERO, Employee::getSalary, BigDecimal::add),
                                Collectors.counting(),
                                (sum, count) -> count > 0
                                        ? sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                                        : BigDecimal.ZERO
                        )
                ));
    }


    public Map<String, Optional<Employee>> getHighestPaidEmployeeByDepartment() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        maxBy(Comparator.comparing(Employee::getSalary))));
    }

    public List<Employee> getTopPaidActiveEmployees(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return employees.stream()
                .filter(Employee::isActive)
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .limit(limit)
                .toList();
    }

    public Map<String, Long> getActiveInactiveCount() {
        return employees.stream()
                .collect(Collectors.groupingBy(e -> e.isActive() ? "active" : "inactive",Collectors.counting()));
    }

    public Map<String, List<String>> getEmployeesGroupedBySkill() {
        return employees.stream()
                .filter(e -> e.getSkills() != null)
                .flatMap(emp -> emp.getSkills().stream()
                        // Map.entry is much cleaner and lighter than AbstractMap.SimpleEntry
                        .map(skill -> Map.entry(skill, emp.getName())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(
                                Map.Entry::getValue,
                                Collectors.toUnmodifiableList() // Cleans up collectingAndThen boilerplate
                        )
                ));
    }

}