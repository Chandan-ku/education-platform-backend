package com.collegenet.collegenet.service;

import com.collegenet.collegenet.dto.AdminReportResponse;
import com.collegenet.collegenet.dto.AdminStudentDTO;
import com.collegenet.collegenet.dto.CollegeApprovalDTO;
import com.collegenet.collegenet.dto.EnrollmentManagementDTO;
import com.collegenet.collegenet.dto.StudentUpdateDTO;
import com.collegenet.collegenet.entity.College;
import com.collegenet.collegenet.entity.Course;
import com.collegenet.collegenet.entity.Student;
import com.collegenet.collegenet.entity.StudentCourse;
import com.collegenet.collegenet.entity.User;
import com.collegenet.collegenet.exception.ResourceNotFoundException;
import com.collegenet.collegenet.repo.CollegeRepo;
import com.collegenet.collegenet.repo.CourseRepo;
import com.collegenet.collegenet.repo.FeeRepo;
import com.collegenet.collegenet.repo.PaymentRepo;
import com.collegenet.collegenet.repo.StudentCourseRepo;
import com.collegenet.collegenet.repo.StudentRepo;
import com.collegenet.collegenet.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CollegeRepo collegeRepository;
    private final UserRepo userRepository;
    private final CourseRepo courseRepository;
    private final StudentRepo studentRepository;
    private final PaymentRepo paymentRepository;
    private final FeeRepo feeRepository;
    private final StudentCourseRepo studentCourseRepo;

    // ==================== Existing Methods ====================

    public List<College> getAllColleges() {
        return collegeRepository.findAll();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public AdminReportResponse getReports() {
        Long totalColleges = collegeRepository.count();
        Long totalStudents = studentRepository.count();
        Long totalCourses = courseRepository.count();
        Long totalUsers = userRepository.count();
        Long totalPayments = paymentRepository.count();

        return AdminReportResponse.builder()
                .totalColleges(totalColleges)
                .totalStudents(totalStudents)
                .totalCourses(totalCourses)
                .totalUsers(totalUsers)
                .totalPayments(totalPayments)
                .build();
    }

    // ==================== Admin Student CRUD ====================

    /**
     * Get all students with enriched admin view
     */
    @Transactional(readOnly = true)
    public List<AdminStudentDTO> getAllStudentsForAdmin() {
        return studentRepository.findAll()
                .stream()
                .map(this::convertToAdminStudentDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get student by ID with enriched admin view
     */
    @Transactional(readOnly = true)
    public AdminStudentDTO getStudentForAdmin(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        return convertToAdminStudentDTO(student);
    }

    /**
     * Update student information by admin
     */
    @Transactional
    public AdminStudentDTO updateStudent(Long studentId, StudentUpdateDTO updateDTO) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        if (updateDTO.getEnrollmentNumber() != null) {
            student.setEnrollmentNumber(updateDTO.getEnrollmentNumber());
        }
        if (updateDTO.getCourse() != null) {
            student.setCourse(updateDTO.getCourse());
        }
        if (updateDTO.getSemester() != null) {
            student.setSemester(updateDTO.getSemester());
        }
        if (updateDTO.getAttendancePercentage() != null) {
            student.setAttendancePercentage(updateDTO.getAttendancePercentage());
        }
        if (updateDTO.getFeesPaid() != null) {
            student.setFeesPaid(updateDTO.getFeesPaid());
        }

        Student updatedStudent = studentRepository.save(student);
        return convertToAdminStudentDTO(updatedStudent);
    }

    /**
     * Delete student by admin (cascade delete all related records)
     */
    @Transactional
    public void deleteStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        // Delete all enrollments first
        studentCourseRepo.deleteAllByStudentId(studentId);

        // Delete all fees and payments
        feeRepository.deleteByStudentId(studentId);
        paymentRepository.deleteAllByStudentId(studentId);

        // Finally delete the student
        studentRepository.delete(student);
    }

    // ==================== College Approval Management ====================

    /**
     * Get all colleges with approval status
     */
    @Transactional(readOnly = true)
    public List<CollegeApprovalDTO> getAllCollegesForApproval() {
        return collegeRepository.findAll()
                .stream()
                .map(this::convertToCollegeApprovalDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get college by ID with approval status
     */
    @Transactional(readOnly = true)
    public CollegeApprovalDTO getCollegeForApproval(Long collegeId) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + collegeId));
        return convertToCollegeApprovalDTO(college);
    }

    /**
     * Approve a college
     */
    @Transactional
    public CollegeApprovalDTO approveCollege(Long collegeId) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + collegeId));

        college.setApproved(true);
        College approvedCollege = collegeRepository.save(college);
        return convertToCollegeApprovalDTO(approvedCollege);
    }

    /**
     * Reject a college (mark as not approved)
     */
    @Transactional
    public CollegeApprovalDTO rejectCollege(Long collegeId) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + collegeId));

        college.setApproved(false);
        College rejectedCollege = collegeRepository.save(college);
        return convertToCollegeApprovalDTO(rejectedCollege);
    }

    /**
     * Get only approved colleges
     */
    @Transactional(readOnly = true)
    public List<College> getApprovedColleges() {
        return collegeRepository.findAll()
                .stream()
                .filter(College::isApproved)
                .collect(Collectors.toList());
    }

    /**
     * Get only pending colleges (not approved)
     */
    @Transactional(readOnly = true)
    public List<CollegeApprovalDTO> getPendingColleges() {
        return collegeRepository.findAll()
                .stream()
                .filter(c -> !c.isApproved())
                .map(this::convertToCollegeApprovalDTO)
                .collect(Collectors.toList());
    }

    // ==================== Enrollment Management ====================

    /**
     * Get all enrollments for admin management
     */
    @Transactional(readOnly = true)
    public List<EnrollmentManagementDTO> getAllEnrollments() {
        return studentCourseRepo.findAll()
                .stream()
                .map(this::convertToEnrollmentManagementDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get enrollments for a specific student
     */
    @Transactional(readOnly = true)
    public List<EnrollmentManagementDTO> getStudentEnrollments(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        return studentCourseRepo.findByStudentId(studentId)
                .stream()
                .map(this::convertToEnrollmentManagementDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get enrollments for a specific course
     */
    @Transactional(readOnly = true)
    public List<EnrollmentManagementDTO> getCourseEnrollments(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        return studentCourseRepo.findByCourseId(courseId)
                .stream()
                .map(this::convertToEnrollmentManagementDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update enrollment status
     */
    @Transactional
    public EnrollmentManagementDTO updateEnrollmentStatus(Long enrollmentId, String newStatus) {
        StudentCourse enrollment = studentCourseRepo.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));

        try {
            StudentCourse.EnrollmentStatus status = StudentCourse.EnrollmentStatus.valueOf(newStatus.toUpperCase());
            enrollment.setStatus(status);
            StudentCourse updatedEnrollment = studentCourseRepo.save(enrollment);
            return convertToEnrollmentManagementDTO(updatedEnrollment);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }
    }

    /**
     * Delete an enrollment
     */
    @Transactional
    public void deleteEnrollment(Long enrollmentId) {
        StudentCourse enrollment = studentCourseRepo.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));
        studentCourseRepo.delete(enrollment);
    }

    // ==================== Helper Methods ====================

    private AdminStudentDTO convertToAdminStudentDTO(Student student) {
        String studentName = (student.getUser() != null && student.getUser().getFullName() != null)
                ? student.getUser().getFullName()
                : "Unknown";

        String collegeName = (student.getCollege() != null && student.getCollege().getCollegeName() != null)
                ? student.getCollege().getCollegeName()
                : "Unknown";

        Long collegeId = (student.getCollege() != null) ? student.getCollege().getId() : null;

        String email = (student.getUser() != null && student.getUser().getEmail() != null)
                ? student.getUser().getEmail()
                : null;

        String phone = (student.getUser() != null && student.getUser().getPhone() != null)
                ? student.getUser().getPhone()
                : null;

        return AdminStudentDTO.builder()
                .id(student.getId())
                .enrollmentNumber(student.getEnrollmentNumber())
                .studentName(studentName)
                .collegeName(collegeName)
                .collegeId(collegeId)
                .course(student.getCourse())
                .semester(student.getSemester())
                .attendancePercentage(student.getAttendancePercentage())
                .feesPaid(student.getFeesPaid())
                .email(email)
                .phone(phone)
                .build();
    }

    private CollegeApprovalDTO convertToCollegeApprovalDTO(College college) {
        return CollegeApprovalDTO.builder()
                .collegeId(college.getId())
                .collegeName(college.getCollegeName())
                .email(college.getEmail())
                .phone(college.getPhone())
                .address(college.getAddress())
                .city(college.getCity())
                .state(college.getState())
                .approved(college.isApproved())
                .build();
    }


    private EnrollmentManagementDTO convertToEnrollmentManagementDTO(StudentCourse enrollment) {
        String studentName = (enrollment.getStudent().getUser() != null && enrollment.getStudent().getUser().getFullName() != null)
                ? enrollment.getStudent().getUser().getFullName()
                : "Unknown";

        return EnrollmentManagementDTO.builder()
                .enrollmentId(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .enrollmentNumber(enrollment.getStudent().getEnrollmentNumber())
                .studentName(studentName)
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .status(enrollment.getStatus().toString())
                .enrollmentDate(enrollment.getEnrollmentDate().toString())
                .build();
    }
}

