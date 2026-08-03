package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class Employee {
    private final String id;
    private final String name;
    private final String department;
    private final int age;
    private final BigDecimal salary;
    private final boolean active;
    private final LocalDate joiningDate;
    private final List<String> skills;

    public Employee(String id, String name, String department, int age, BigDecimal salary, boolean active,
                    LocalDate joiningDate, List<String> skills) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Employee ID cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Employee name cannot be null or blank");
        }
        if (department == null || department.isBlank()) {
            throw new IllegalArgumentException("Department cannot be null or blank");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        if (salary == null) {
            throw new IllegalArgumentException("Salary cannot be null");
        }
        if (salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        if (joiningDate == null) {
            throw new IllegalArgumentException("Joining date cannot be null");
        }
        if (skills == null) {
            throw new IllegalArgumentException("Skills list cannot be null");
        }
        this.id = id;
        this.name = name;
        this.department = department;
        this.age = age;
        this.salary = salary;
        this.active = active;
        this.joiningDate = joiningDate;
        this.skills = List.copyOf(skills);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public int getAge() {
        return age;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public List<String> getSkills() {
        return skills;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee employee)) return false;
        return age == employee.age &&
                active == employee.active &&
                id.equals(employee.id) &&
                name.equals(employee.name) &&
                department.equals(employee.department) &&
                salary.equals(employee.salary) &&
                joiningDate.equals(employee.joiningDate) &&
                skills.equals(employee.skills);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, department, age, salary, active, joiningDate, skills);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", age=" + age +
                ", salary=" + salary +
                ", active=" + active +
                ", joiningDate=" + joiningDate +
                ", skills=" + skills +
                '}';
    }
}