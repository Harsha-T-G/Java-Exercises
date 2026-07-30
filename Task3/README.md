# Bank Account Management System

This is a simple console-based Bank Account Management System implemented in Java 17/21 using Maven and JUnit 5. No Spring Boot or database is used.

## Project Structure

- `src/main/java`: Contains the Java source code.
- `src/test/java`: Contains the JUnit 5 test classes.

## How to Build and Run

### Prerequisites
- Java JDK 17 or 21
- Maven 3.6+

### Build
```bash
mvn clean install
```

### Run Tests
```bash
mvn test
```

### Run the Application (if a main class is provided)
```bash
mvn compile exec:java -Dexec.mainClass="com.example.Main"
```
(Adjust the main class as per your implementation.)

## Design Overview

Please refer to `CLAUDE.md` for detailed explanation of the design techniques used, including encapsulation, abstraction, immutability, exception handling, and separation of responsibilities.

## License

This project is for educational purposes.