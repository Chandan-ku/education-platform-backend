package com.collegenet.collegenet.service;

import com.collegenet.collegenet.dto.CourseDTO;
import com.collegenet.collegenet.dto.StudentResponseDTO;
import com.collegenet.collegenet.entity.Course;
import com.collegenet.collegenet.entity.Student;
import com.collegenet.collegenet.entity.StudentCourse;
import com.collegenet.collegenet.exception.ResourceNotFoundException;
import com.collegenet.collegenet.repo.CourseRepo;
import com.collegenet.collegenet.repo.StudentCourseRepo;
import com.collegenet.collegenet.repo.StudentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final StudentCourseRepo studentCourseRepo;
    private final StudentRepo studentRepository;
    private final CourseRepo courseRepository;

    /**
     * Enroll a student to a course
     * Prevents duplicate enrollment
     */
    @Transactional
    public StudentCourse enrollStudentToCourse(Long studentId, Long courseId) {
        // Validate student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        // Validate course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Prevent duplicate enrollment
        if (studentCourseRepo.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new IllegalArgumentException("Student is already enrolled in this course");
        }

        // Create and save enrollment
        StudentCourse enrollment = StudentCourse.builder()
                .student(student)
                .course(course)
                .enrollmentDate(LocalDate.now())
                .status(StudentCourse.EnrollmentStatus.ACTIVE)
                .build();

        return studentCourseRepo.save(enrollment);
    }

    /**
     * Get all courses for a student
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getStudentCourses(Long studentId) {
        // Validate student exists
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }

        return studentCourseRepo.findByStudentId(studentId)
                .stream()
                .filter(enrollment -> enrollment.getStatus() != StudentCourse.EnrollmentStatus.DROPPED)
                .map(this::convertCourseToCourseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all students enrolled in a course
     */
    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getCourseStudents(Long courseId) {
        // Validate course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        return studentCourseRepo.findByCourseId(courseId)
                .stream()
                .filter(enrollment -> enrollment.getStatus() != StudentCourse.EnrollmentStatus.DROPPED)
                .map(enrollment -> convertStudentToStudentResponseDTO(enrollment.getStudent()))
                .collect(Collectors.toList());
    }

    /**
     * Drop a course for a student
     * Updates enrollment status to DROPPED instead of deleting
     */
    @Transactional
    public void dropCourse(Long studentId, Long courseId) {
        StudentCourse enrollment = studentCourseRepo.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found for student: " + studentId + ", course: " + courseId));

        enrollment.setStatus(StudentCourse.EnrollmentStatus.DROPPED);
        studentCourseRepo.save(enrollment);
    }

    /**
     * Get enrollment status for a student-course pair
     */
    @Transactional(readOnly = true)
    public StudentCourse.EnrollmentStatus getEnrollmentStatus(Long studentId, Long courseId) {
        StudentCourse enrollment = studentCourseRepo.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found for student: " + studentId + ", course: " + courseId));

        return enrollment.getStatus();
    }

    /**
     * Get all active enrollments for a student
     */
    @Transactional(readOnly = true)
    public List<StudentCourse> getActiveEnrollments(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }

        return studentCourseRepo.findByStudentId(studentId)
                .stream()
                .filter(enrollment -> enrollment.getStatus() == StudentCourse.EnrollmentStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    /**
     * Get total number of enrolled students in a course
     */
    @Transactional(readOnly = true)
    public long getEnrolledStudentCount(Long courseId) {
        return studentCourseRepo.findByCourseId(courseId)
                .stream()
                .filter(enrollment -> enrollment.getStatus() == StudentCourse.EnrollmentStatus.ACTIVE)
                .count();
    }

    // Helper method to convert Course to CourseDTO
    private CourseDTO convertCourseToCourseDTO(StudentCourse enrollment) {
        Course course = enrollment.getCourse();
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setPrice(course.getPrice());
        dto.setJobGuaranteed(course.getJobGuaranteed());
        dto.setDurationInWeeks(course.getDurationInWeeks());
        return dto;
    }

    // Helper method to convert Student to StudentResponseDTO
    private StudentResponseDTO convertStudentToStudentResponseDTO(Student student) {
        return StudentResponseDTO.builder()
                .id(student.getId())
                .enrollmentNumber(student.getEnrollmentNumber())
                .course(student.getCourse())
                .semester(student.getSemester())
                .attendancePercentage(student.getAttendancePercentage())
                .feesPaid(student.getFeesPaid())
                .build();
    }
}

