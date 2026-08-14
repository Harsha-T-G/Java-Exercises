package org.example;

import org.example.courseenrollment.domain.Course;
import org.example.courseenrollment.domain.Student;
import org.example.courseenrollment.service.CourseEnrollmentService;

public class Main {

    public static void main(String[] args) {
        CourseEnrollmentService enrollmentService = new CourseEnrollmentService();

        enrollmentService.addStudent(new Student("S-101", "Aarav Sharma"));
        enrollmentService.addStudent(new Student("S-102", "Meera Patel"));
        enrollmentService.addStudent(new Student("S-103", "Kabir Singh"));

        enrollmentService.addCourse(new Course("C-101", "Data Structures", 2));
        enrollmentService.addCourse(new Course("C-102", "Java Programming", 3));

        enrollmentService.enroll("S-101", "C-101");
        enrollmentService.enroll("S-102", "C-101");
        enrollmentService.enroll("S-101", "C-102");
        enrollmentService.enroll("S-103", "C-102");

        System.out.println("Students enrolled in Data Structures:");
        enrollmentService.getStudentsForCourse("C-101").forEach(System.out::println);

        System.out.println("\nCourses for Aarav Sharma:");
        enrollmentService.getCoursesForStudent("S-101").forEach(System.out::println);

        System.out.println("\nAvailable seats in Data Structures: "
                + enrollmentService.getAvailableSeats("C-101"));

        enrollmentService.withdraw("S-102", "C-101");
        System.out.println("Available seats after Meera withdraws: "
                + enrollmentService.getAvailableSeats("C-101"));
    }
}
