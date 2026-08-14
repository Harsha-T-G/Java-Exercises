package org.example;

import org.example.employeedirectory.domain.Employee;
import org.example.employeedirectory.service.EmployeeDirectory;

import java.util.Set;

public class Main {

    public static void main(String[] args) {
        EmployeeDirectory directory = new EmployeeDirectory();

        directory.addEmployee(new Employee(
                "E-101", "Asha", "Engineering", Set.of("Java", "SQL")));
        directory.addEmployee(new Employee(
                "E-102", "Ben", "Finance", Set.of("Excel", "SQL")));
        directory.addEmployee(new Employee(
                "E-103", "Zoya", "Engineering", Set.of("Java", "Python")));
        directory.addEmployee(new Employee(
                "E-104", "Ravi", "Human Resources", null));

        System.out.println("Employee E-101:");
        System.out.println(directory.findById("E-101"));

        System.out.println("\nEngineering employees:");
        directory.findByDepartment("Engineering").forEach(System.out::println);

        System.out.println("\nEmployees with the JAVA skill:");
        directory.findBySkill("JAVA").forEach(System.out::println);

        System.out.println("\nEmployees grouped by department:");
        directory.groupByDepartment().forEach((department, employees) ->
                System.out.println(department + ": " + employees));

        System.out.println("\nAll skills: " + directory.getAllSkills());

        directory.removeEmployee("E-102");
        System.out.println("\nRemoved employee E-102.");
    }
}
