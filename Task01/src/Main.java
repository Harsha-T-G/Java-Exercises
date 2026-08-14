import java.util.*;
import java.util.stream.Collectors;

void main() {
    Employee emp = new Employee();
    emp.setId(9);
    emp.setAge(21);
    emp.setDepartment("IT");
    emp.setName("Harsha");
    emp.setSalary(20000);
    List<Employee> employees = new ArrayList<>(
            Arrays.asList(
                    new Employee(1, "Ashik", "IT", 70000, 26),
                    new Employee(2, "Rahul", "HR", 50000, 30),
                    new Employee(3, "John", "IT", 90000, 35),
                    new Employee(4, "Sneha", "Finance", 80000, 28),
                    new Employee(5, "Anu", "IT", 60000, 24),
                    new Employee(6, "Vivek", "HR", 55000, 31),
                    new Employee(7, "David", "Finance", 95000, 40),
                    new Employee(8, "Arun", "IT", 72000, 29)
            )
    );
    employees.add(emp);
//       employees.forEach(System.out::println);


    System.out.println("1. Given a list of Employee objects, filter all employees whose salary is greater than 70,000 and collect the result into a new List");
    System.out.println(getEmployeeSalaryGreaterThan70000(employees));
    System.out.println();

    System.out.println("2. Given a list of Employee objects, group all employees by their department and return a Map<String, List<Employee>> ");
    System.out.println(getMapOfEmployeeOnDepartment(employees));
    System.out.println();

    System.out.println("3. Given a list of Employee objects, create a Map<Integer, String> where the key is the employee ID and the value is the employee name.");
    System.out.println(getEmployeeNameOnEmployeeId(employees));
    System.out.println();

    System.out.println("4. Given a list of Employee objects, find the employee with the highest salary using Streams.");
    System.out.println(getMaxEmployeeBySalary(employees));
    System.out.println();

    System.out.println("5. Given a list of Employee objects, calculate the average salary of all employees.");
    System.out.println(getAverageSalary(employees));
    System.out.println();

    System.out.println("6. Given a list of Employee objects, sort the employees by salary in descending order and return the top 5 highest-paid employees.");
    System.out.println(getTopFiveSalary(employees));
    System.out.println();

    System.out.println("7. Given a list of Employee objects, find all distinct department names and return them as a List<String>.");
    System.out.println(getDistinctDepartment(employees));
    System.out.println();

//    Expected Output:
//    IT -> 292000
//    HR -> 105000
//    Finance -> 175000
    System.out.println("8. Given a list of Employee objects, calculate the total salary paid for each department.");
    System.out.println(getSalaryByDept(employees));
    System.out.println();

    System.out.println("9. Given a list of Employee objects, find all employees whose salary is greater than the average salary of all employees.");
    System.out.println(getEmployeeSalaryAboveAverage(employees));
    System.out.println();

    System.out.println("10. Given a list of Employee objects, find the second highest-paid employee using Java 8 Streams without using loops.");
    System.out.println(getSecondHighestSalaryEmployee(employees));
}


public static List<Employee> getEmployeeSalaryGreaterThan70000(List<Employee> emp) {
    return emp.stream().filter(e -> e.getSalary() > 70000).toList();
}

public static Map<String, List<Employee>> getMapOfEmployeeOnDepartment(List<Employee> emp) {
    return emp.stream().collect(Collectors.groupingBy(Employee::getDepartment));
}

public static Map<Integer, String> getEmployeeNameOnEmployeeId(List<Employee> emp) {
    return emp.stream().collect(Collectors.toMap(Employee::getId, Employee::getName));
}

public static Optional<Employee> getMaxEmployeeBySalary(List<Employee> emp) {
    return emp.stream().max(Comparator.comparing(Employee::getSalary));
}

public static double getAverageSalary(List<Employee> emp) {
    return emp.stream().mapToDouble(Employee::getSalary).average().orElse(0.0);
}

public static List<Employee> getTopFiveSalary(List<Employee> emp) {
    return emp.stream().sorted(Comparator.comparing(Employee::getSalary).reversed()).limit(5).toList();
}

public static List<String> getDistinctDepartment(List<Employee> emp) {
    return emp.stream().map(Employee::getDepartment).distinct().toList();
}

public static Map<String, Double> getSalaryByDept(List<Employee> emp) {
    return emp.stream().collect(Collectors.groupingBy(Employee::getDepartment, Collectors.summingDouble(Employee::getSalary)));
}

public static List<Employee> getEmployeeSalaryAboveAverage(List<Employee> emp) {
    double avgSalary = emp.stream().mapToDouble(Employee::getSalary).average().orElse(0.0);
    return emp.stream().filter(e -> e.getSalary() > avgSalary).toList();
}

public static Optional<Employee> getSecondHighestSalaryEmployee(List<Employee> emp) {
    return emp.stream()
            .sorted(Comparator.comparingDouble(Employee::getSalary).reversed())
            .skip(1)
            .findFirst();
}



