package org.example;

import org.example.model.Employee;
import org.example.service.EmployeeDataService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    static void main() {
        // Create sample data directly (copied from SampleData for demo purposes)
        List<Employee> employees = new ArrayList<>();

        // Engineering
        employees.add(new Employee("E001", "Alice Johnson", "Engineering", 29,
                new BigDecimal("75000.00"), true, LocalDate.of(2022, 3, 15),
                List.of("Java", "Spring", "AWS")));
        employees.add(new Employee("E002", "Bob Smith", "Engineering", 35,
                new BigDecimal("95000.00"), true, LocalDate.of(2021, 7, 22),
                List.of("Python", "Django", "PostgreSQL")));
        employees.add(new Employee("E003", "Carol Davis", "Engineering", 27,
                new BigDecimal("82000.00"), false, LocalDate.of(2023, 1, 10),
                List.of("C++", "Linux", "Git")));

        // Marketing
        employees.add(new Employee("M001", "David Brown", "Marketing", 30,
                new BigDecimal("60000.00"), true, LocalDate.of(2022, 11, 5),
                List.of("SEO", "Google Analytics", "Content Marketing")));
        employees.add(new Employee("M002", "Eve Wilson", "Marketing", 28,
                new BigDecimal("58000.00"), true, LocalDate.of(2025, 2, 18),
                List.of("Social Media", "Copywriting", "Email Campaigns")));
        employees.add(new Employee("M003", "Frank Miller", "Marketing", 32,
                new BigDecimal("62000.00"), false, LocalDate.of(2020, 6, 30),
                List.of("Market Research", "AdWords", "Analytics")));

        // Sales
        employees.add(new Employee("S001", "Grace Lee", "Sales", 34,
                new BigDecimal("70000.00"), true, LocalDate.of(2021, 9, 12),
                List.of("CRM", "Negotiation", "Presentation Skills")));
        employees.add(new Employee("S002", "Henry Taylor", "Sales", 29,
                new BigDecimal("68000.00"), true, LocalDate.of(2022, 4, 3),
                List.of("Lead Generation", "Cold Calling", "Salesforce")));
        employees.add(new Employee("S003", "Ivy Clark", "Sales", 31,
                new BigDecimal("72000.00"), false, LocalDate.of(2020, 8, 19),
                List.of("Account Management", "Forecasting", "Territory Planning")));

        // HR
        employees.add(new Employee("H001", "Jack Wilson", "HR", 28,
                new BigDecimal("55000.00"), true, LocalDate.of(2023, 1, 20),
                List.of("Recruiting", "Onboarding", "HRIS")));
        employees.add(new Employee("H002", "Karen White", "HR", 33,
                new BigDecimal("57000.00"), true, LocalDate.of(2025, 8, 14),
                List.of("Employee Relations", "Performance Management", "Training")));
        employees.add(new Employee("H003", "Liam Martinez", "HR", 26,
                new BigDecimal("53000.00"), false, LocalDate.of(2021, 5, 25),
                List.of("Payroll", "Benefits Administration", "Compliance")));

        // Additional employees to exceed 15
        employees.add(new Employee("E004", "Nina Patel", "Engineering", 26,
                new BigDecimal("78000.00"), true, LocalDate.of(2023, 6, 10),
                List.of("Python", "AWS", "React")));
        employees.add(new Employee("M004", "Oscar Ng", "Marketing", 28,
                new BigDecimal("61000.00"), false, LocalDate.of(2024, 11, 3),
                List.of("Email Marketing", "Copywriting", "Analytics")));

        EmployeeDataService service = new EmployeeDataService(employees);

        System.out.println("=== Employee Data Analysis Demo ===\n");

        // 1. Active employees sorted by salary descending
        System.out.println("1. Active Employees sorted by Salary (Descending):");
        service.getActiveEmployeesSortedBySalaryDesc()
            .forEach(e -> System.out.printf("  %s (%s): $%s%n",
                e.getName(), e.getDepartment(), e.getSalary()));
        System.out.println();

        // 2. Employees joined in last 2 years
        System.out.println("2. Employees Joined in Last 2 Years:");
        service.getEmployeesJoinedLastTwoYears()
            .forEach(e -> System.out.printf("  %s (%s): %s%n",
                e.getName(), e.getDepartment(), e.getJoiningDate()));
        System.out.println();

        // 3. Unique skills sorted alphabetically
        System.out.println("3. Unique Skills (Alphabetically Sorted):");
        service.getUniqueSkillsSorted()
            .forEach(skill -> System.out.print(skill+" , "));
        System.out.println("\n");

        // 4. Employees grouped by department
        System.out.println("4. Employees Grouped by Department:");
        Map<String, List<Employee>> deptMap = service.getEmployeesGroupedByDepartment();
        deptMap.forEach((dept, list) -> {
            System.out.printf("  %s (%d employees):%n", dept, list.size());
            list.forEach(e -> System.out.printf("    - %s (%s)%n", e.getName(), e.getSalary()));
        });
        System.out.println();

        // 5. Average salary by department
        System.out.println("5. Average Salary by Department:");
        Map<String, BigDecimal> avgMap = service.getAverageSalaryByDepartment();
        avgMap.forEach((dept, avg) ->
            System.out.printf("  %s: $%s%n", dept, avg));
        System.out.println();

        // 6. Highest paid employee by department
        System.out.println("6. Highest Paid Employee by Department:");
        Map<String, java.util.Optional<Employee>> highestMap = service.getHighestPaidEmployeeByDepartment();
        highestMap.forEach((dept, opt) ->
            opt.ifPresentOrElse(e ->
                System.out.printf("  %s: %s ($%s)%n", dept, e.getName(), e.getSalary()),
                () -> System.out.printf("  %s: No employees%n", dept)));
        System.out.println();

        // 7. Top 3 highest-paid active employees
        System.out.println("7. Top 3 Highest-Paid Active Employees:");
        service.getTopPaidActiveEmployees(3)
            .forEach(e -> System.out.printf("  %s (%s): $%s%n",
                e.getName(), e.getDepartment(), e.getSalary()));
        System.out.println();

        //8. Count the number of active and inactive employees.
        System.out.println("8. Count the number of active and inactive employees.");
        Map<String,Long> actEmp = service.getActiveInactiveCount();
        actEmp.forEach((status,count)-> System.out.printf(" %-12s : %,d %n",status,count));
        System.out.println();

        // 9. Employees grouped by skill
        System.out.println("9. Employees Grouped by Skill:");
        Map<String, List<String>> skillMap = service.getEmployeesGroupedBySkill();
        skillMap.forEach((skill, names) -> System.out.printf("  %s: %s%n", skill, String.join(", ", names)));
        System.out.println();

        System.out.println("=== Demo Complete ===");
    }
}