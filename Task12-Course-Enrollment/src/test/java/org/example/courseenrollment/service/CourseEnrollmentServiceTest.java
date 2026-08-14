package org.example.courseenrollment.service;

import org.example.courseenrollment.domain.Course;
import org.example.courseenrollment.domain.Student;
import org.example.courseenrollment.exception.CourseFullException;
import org.example.courseenrollment.exception.CourseNotFoundException;
import org.example.courseenrollment.exception.DuplicateCourseException;
import org.example.courseenrollment.exception.DuplicateEnrollmentException;
import org.example.courseenrollment.exception.DuplicateStudentException;
import org.example.courseenrollment.exception.EnrollmentNotFoundException;
import org.example.courseenrollment.exception.InvalidCourseException;
import org.example.courseenrollment.exception.InvalidStudentException;
import org.example.courseenrollment.exception.StudentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CourseEnrollmentServiceTest {

    private CourseEnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        enrollmentService = new CourseEnrollmentService();
    }

    @Test
    void givenValidStudentsAndCourses_whenAdded_thenTheyCanBeUsedForEnrollment() {
        Student student = student("S-1", "Aarav");
        Course course = course("C-1", "Java", 2);
        enrollmentService.addStudent(student);
        enrollmentService.addCourse(course);

        enrollmentService.enroll(student.studentId(), course.courseId());

        assertEquals(List.of(student), enrollmentService.getStudentsForCourse("C-1"));
        assertEquals(List.of(course), enrollmentService.getCoursesForStudent("S-1"));
    }

    @Test
    void givenInvalidStudentData_whenAdded_thenRejectsIt() {
        assertThrows(InvalidStudentException.class, () -> enrollmentService.addStudent(null));
        assertThrows(InvalidStudentException.class,
                () -> enrollmentService.addStudent(student(" ", "Aarav")));
        assertThrows(InvalidStudentException.class,
                () -> enrollmentService.addStudent(student("S-1", null)));
        assertThrows(InvalidStudentException.class,
                () -> enrollmentService.addStudent(student("S-1", " ")));
    }

    @Test
    void givenDuplicateStudentId_whenAdded_thenRejectsIt() {
        enrollmentService.addStudent(student("S-1", "Aarav"));

        assertThrows(DuplicateStudentException.class,
                () -> enrollmentService.addStudent(student("S-1", "Meera")));
    }

    @Test
    void givenInvalidCourseData_whenAdded_thenRejectsIt() {
        assertThrows(InvalidCourseException.class, () -> enrollmentService.addCourse(null));
        assertThrows(InvalidCourseException.class,
                () -> enrollmentService.addCourse(course(" ", "Java", 1)));
        assertThrows(InvalidCourseException.class,
                () -> enrollmentService.addCourse(course("C-1", null, 1)));
        assertThrows(InvalidCourseException.class,
                () -> enrollmentService.addCourse(course("C-1", " ", 1)));
        assertThrows(InvalidCourseException.class,
                () -> enrollmentService.addCourse(course("C-1", "Java", 0)));
        assertThrows(InvalidCourseException.class,
                () -> enrollmentService.addCourse(course("C-1", "Java", -1)));
    }

    @Test
    void givenDuplicateCourseId_whenAdded_thenRejectsIt() {
        enrollmentService.addCourse(course("C-1", "Java", 2));

        assertThrows(DuplicateCourseException.class,
                () -> enrollmentService.addCourse(course("C-1", "Databases", 3)));
    }

    @Test
    void givenExistingEnrollment_whenEnrolledAgain_thenRejectsDuplicate() {
        addStudentAndCourse("S-1", "Aarav", "C-1", "Java", 2);
        enrollmentService.enroll("S-1", "C-1");

        assertThrows(DuplicateEnrollmentException.class,
                () -> enrollmentService.enroll("S-1", "C-1"));
        assertEquals(1, enrollmentService.getStudentsForCourse("C-1").size());
    }

    @Test
    void givenFullCourse_whenAnotherStudentEnrolls_thenRejectsAndKeepsEnrollmentUnchanged() {
        addStudentAndCourse("S-1", "Aarav", "C-1", "Java", 1);
        enrollmentService.addStudent(student("S-2", "Meera"));
        enrollmentService.enroll("S-1", "C-1");

        assertThrows(CourseFullException.class,
                () -> enrollmentService.enroll("S-2", "C-1"));
        assertEquals(List.of(student("S-1", "Aarav")),
                enrollmentService.getStudentsForCourse("C-1"));
        assertEquals(0, enrollmentService.getAvailableSeats("C-1"));
    }

    @Test
    void givenMissingStudentOrCourse_whenEnrollmentIsAttempted_thenRejectsIt() {
        enrollmentService.addCourse(course("C-1", "Java", 2));

        assertThrows(StudentNotFoundException.class,
                () -> enrollmentService.enroll("missing", "C-1"));

        enrollmentService.addStudent(student("S-1", "Aarav"));
        assertThrows(CourseNotFoundException.class,
                () -> enrollmentService.enroll("S-1", "missing"));
    }

    @Test
    void givenEnrolledStudent_whenWithdrawn_thenRemovesEnrollmentAndFreesSeat() {
        addStudentAndCourse("S-1", "Aarav", "C-1", "Java", 2);
        enrollmentService.enroll("S-1", "C-1");

        enrollmentService.withdraw("S-1", "C-1");

        assertEquals(List.of(), enrollmentService.getStudentsForCourse("C-1"));
        assertEquals(2, enrollmentService.getAvailableSeats("C-1"));
    }

    @Test
    void givenNoEnrollment_whenWithdrawalIsAttempted_thenRejectsIt() {
        addStudentAndCourse("S-1", "Aarav", "C-1", "Java", 2);

        assertThrows(EnrollmentNotFoundException.class,
                () -> enrollmentService.withdraw("S-1", "C-1"));
    }

    @Test
    void givenMissingStudentOrCourse_whenWithdrawalIsAttempted_thenRejectsIt() {
        enrollmentService.addCourse(course("C-1", "Java", 2));

        assertThrows(StudentNotFoundException.class,
                () -> enrollmentService.withdraw("missing", "C-1"));

        enrollmentService.addStudent(student("S-1", "Aarav"));
        assertThrows(CourseNotFoundException.class,
                () -> enrollmentService.withdraw("S-1", "missing"));
    }

    @Test
    void givenCourseEnrollments_whenSeatsAreRequested_thenReturnsRemainingCapacity() {
        addStudentAndCourse("S-1", "Aarav", "C-1", "Java", 3);
        enrollmentService.addStudent(student("S-2", "Meera"));

        assertEquals(3, enrollmentService.getAvailableSeats("C-1"));

        enrollmentService.enroll("S-1", "C-1");
        enrollmentService.enroll("S-2", "C-1");

        assertEquals(1, enrollmentService.getAvailableSeats("C-1"));
    }

    @Test
    void givenSeveralEnrollments_whenStudentsAreRequested_thenSortsThemByName() {
        Student meera = student("S-1", "Meera");
        Student aarav = student("S-2", "Aarav");
        Student kabir = student("S-3", "Kabir");
        enrollmentService.addStudent(meera);
        enrollmentService.addStudent(aarav);
        enrollmentService.addStudent(kabir);
        enrollmentService.addCourse(course("C-1", "Java", 3));
        enrollmentService.enroll("S-1", "C-1");
        enrollmentService.enroll("S-2", "C-1");
        enrollmentService.enroll("S-3", "C-1");

        List<Student> result = enrollmentService.getStudentsForCourse("C-1");

        assertEquals(List.of(aarav, kabir, meera), result);
    }

    @Test
    void givenStudentEnrollments_whenCoursesAreRequested_thenSortsThemByTitle() {
        Student student = student("S-1", "Aarav");
        Course networks = course("C-1", "Networks", 2);
        Course algorithms = course("C-2", "Algorithms", 2);
        Course databases = course("C-3", "Databases", 2);
        enrollmentService.addStudent(student);
        enrollmentService.addCourse(networks);
        enrollmentService.addCourse(algorithms);
        enrollmentService.addCourse(databases);
        enrollmentService.enroll("S-1", "C-1");
        enrollmentService.enroll("S-1", "C-2");
        enrollmentService.enroll("S-1", "C-3");

        List<Course> result = enrollmentService.getCoursesForStudent("S-1");

        assertEquals(List.of(algorithms, databases, networks), result);
    }

    @Test
    void givenQueryResults_whenModificationIsAttempted_thenListsAreImmutable() {
        addStudentAndCourse("S-1", "Aarav", "C-1", "Java", 2);
        enrollmentService.enroll("S-1", "C-1");

        List<Student> students = enrollmentService.getStudentsForCourse("C-1");
        List<Course> courses = enrollmentService.getCoursesForStudent("S-1");

        assertThrows(UnsupportedOperationException.class, students::clear);
        assertThrows(UnsupportedOperationException.class, courses::clear);
    }

    @Test
    void givenMissingStudentOrCourse_whenQueried_thenRejectsIt() {
        assertThrows(CourseNotFoundException.class,
                () -> enrollmentService.getStudentsForCourse("missing"));
        assertThrows(StudentNotFoundException.class,
                () -> enrollmentService.getCoursesForStudent("missing"));
        assertThrows(CourseNotFoundException.class,
                () -> enrollmentService.getAvailableSeats("missing"));
    }

    private void addStudentAndCourse(
            String studentId,
            String studentName,
            String courseId,
            String courseTitle,
            int capacity) {
        enrollmentService.addStudent(student(studentId, studentName));
        enrollmentService.addCourse(course(courseId, courseTitle, capacity));
    }

    private static Student student(String studentId, String name) {
        return new Student(studentId, name);
    }

    private static Course course(String courseId, String title, int capacity) {
        return new Course(courseId, title, capacity);
    }
}
