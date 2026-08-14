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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CourseEnrollmentService {

    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, Course> courses = new HashMap<>();
    private final Map<String, Set<String>> enrollments = new HashMap<>();

    public void addStudent(Student student) {
        validateStudent(student);
        if (students.containsKey(student.studentId())) {
            throw new DuplicateStudentException(
                    "Student ID already exists: " + student.studentId());
        }
        students.put(student.studentId(), student);
    }

    public void addCourse(Course course) {
        validateCourse(course);
        if (courses.containsKey(course.courseId())) {
            throw new DuplicateCourseException(
                    "Course ID already exists: " + course.courseId());
        }
        courses.put(course.courseId(), course);
    }

    public void enroll(String studentId, String courseId) {
        requireStudentExists(studentId);
        Course course = findCourse(courseId);
        Set<String> enrolledStudentIds = enrollments.computeIfAbsent(
                courseId, ignored -> new LinkedHashSet<>());
        if (!enrolledStudentIds.add(studentId)) {
            throw new DuplicateEnrollmentException(
                    "Student is already enrolled in course: " + courseId);
        }
        if (enrolledStudentIds.size() > course.capacity()) {
            enrolledStudentIds.remove(studentId);
            throw new CourseFullException("Course is full: " + courseId);
        }
    }

    public void withdraw(String studentId, String courseId) {
        requireStudentExists(studentId);
        findCourse(courseId);
        Set<String> enrolledStudentIds = enrollments.get(courseId);

        if (enrolledStudentIds == null || !enrolledStudentIds.remove(studentId)) {
            throw new EnrollmentNotFoundException(
                    "Student is not enrolled in course: " + courseId);
        }
    }

    public List<Student> getStudentsForCourse(String courseId) {
        findCourse(courseId);
        List<Student> enrolledStudents = new ArrayList<>();
        Set<String> enrolledStudentIds = enrollments.get(courseId);

        if (enrolledStudentIds != null) {
            for (String studentId : enrolledStudentIds) {
                enrolledStudents.add(students.get(studentId));
            }
        }
        enrolledStudents.sort(Comparator.comparing(Student::name));
        return List.copyOf(enrolledStudents);
    }

    public List<Course> getCoursesForStudent(String studentId) {
        requireStudentExists(studentId);
        List<Course> enrolledCourses = new ArrayList<>();

        for (Course course : courses.values()) {
            Set<String> enrolledStudentIds = enrollments.get(course.courseId());
            if (enrolledStudentIds != null && enrolledStudentIds.contains(studentId)) {
                enrolledCourses.add(course);
            }
        }
        enrolledCourses.sort(Comparator.comparing(Course::title));
        return List.copyOf(enrolledCourses);
    }

    public int getAvailableSeats(String courseId) {
        Course course = findCourse(courseId);
        Set<String> enrolledStudentIds = enrollments.get(courseId);
        int enrollmentCount = enrolledStudentIds == null ? 0 : enrolledStudentIds.size();
        return course.capacity() - enrollmentCount;
    }

    private void requireStudentExists(String studentId) {
        if (!students.containsKey(studentId)) {
            throw new StudentNotFoundException("Student not found: " + studentId);
        }
    }

    private Course findCourse(String courseId) {
        Course course = courses.get(courseId);
        if (course == null) {
            throw new CourseNotFoundException("Course not found: " + courseId);
        }
        return course;
    }

    private static void validateStudent(Student student) {
        if (student == null) {
            throw new InvalidStudentException("Student must not be null");
        }
        if (isBlank(student.studentId())) {
            throw new InvalidStudentException("Student ID must not be null or blank");
        }
        if (isBlank(student.name())) {
            throw new InvalidStudentException("Student name must not be null or blank");
        }
    }

    private static void validateCourse(Course course) {
        if (course == null) {
            throw new InvalidCourseException("Course must not be null");
        }
        if (isBlank(course.courseId())) {
            throw new InvalidCourseException("Course ID must not be null or blank");
        }
        if (isBlank(course.title())) {
            throw new InvalidCourseException("Course title must not be null or blank");
        }
        if (course.capacity() <= 0) {
            throw new InvalidCourseException("Course capacity must be greater than zero");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
