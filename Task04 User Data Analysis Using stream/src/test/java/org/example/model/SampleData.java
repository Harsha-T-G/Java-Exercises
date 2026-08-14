package org.example.model;

import org.example.model.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to create sample employee data for testing.
 */
public class SampleData {

    public static List<Employee> createSampleEmployees() {
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
                new BigDecimal("58000.00"), true, LocalDate.of(2023, 2, 18),
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
                new BigDecimal("57000.00"), true, LocalDate.of(2022, 8, 14),
                List.of("Employee Relations", "Performance Management", "Training")));
        employees.add(new Employee("H003", "Liam Martinez", "HR", 26,
                new BigDecimal("53000.00"), false, LocalDate.of(2021, 5, 25),
                List.of("Payroll", "Benefits Administration", "Compliance")));

        // Additional employees to exceed 15
        employees.add(new Employee("E004", "Nina Patel", "Engineering", 26,
                new BigDecimal("78000.00"), true, LocalDate.of(2023, 6, 10),
                List.of("Python", "AWS", "React")));
        employees.add(new Employee("M004", "Oscar Ng", "Marketing", 28,
                new BigDecimal("61000.00"), false, LocalDate.of(2022, 11, 3),
                List.of("Email Marketing", "Copywriting", "Analytics")));

        return employees;
    }
}