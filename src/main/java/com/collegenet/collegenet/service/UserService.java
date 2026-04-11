package com.collegenet.collegenet.service;

import com.collegenet.collegenet.entity.User;
import com.collegenet.collegenet.entity.Student;
import com.collegenet.collegenet.entity.College;
import com.collegenet.collegenet.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepo userRepository;
    private final StudentRepo studentRepository;
    private final CollegeRepo collegeRepository;
    private final PaymentRepo paymentRepository;
    private final FeeRepo feeRepository;
    private final ResultRepo resultRepository;
    private final MarksRepo marksRepository;
    private final StudentCourseRepo studentCourseRepository;

    public UserService(UserRepo userRepository,
                      StudentRepo studentRepository,
                      CollegeRepo collegeRepository,
                      PaymentRepo paymentRepository,
                      FeeRepo feeRepository,
                      ResultRepo resultRepository,
                      MarksRepo marksRepository,
                      StudentCourseRepo studentCourseRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.collegeRepository = collegeRepository;
        this.paymentRepository = paymentRepository;
        this.feeRepository = feeRepository;
        this.resultRepository = resultRepository;
        this.marksRepository = marksRepository;
        this.studentCourseRepository = studentCourseRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * ✅ SAFE user deletion with cascading child records
     * Deletes in correct order to avoid FK constraint violations:
     * 1. Delete leaf records (Payments, Marks, Results, Fees, StudentCourses)
     * 2. Delete parent records (Student)
     * 3. Delete sibling parent record (College if applicable)
     * 4. Finally delete User
     */
    @Transactional
    public void deleteUserWithCascade(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        logger.info("Starting safe deletion for user id={}, role={}", userId, user.getRole());

        // 1. Handle STUDENT user
        Student student = studentRepository.findByUserId(userId).orElse(null);
        if (student != null) {
            Long studentId = student.getId();
            logger.info("Deleting student records for student id={}", studentId);

            try {
                // Delete leaf records in correct order (no other dependencies)
                paymentRepository.deleteAllByStudentId(studentId);
                logger.debug("Deleted payments for student id={}", studentId);

                marksRepository.deleteAllByStudentId(studentId);
                logger.debug("Deleted marks for student id={}", studentId);

                resultRepository.deleteAllByStudentId(studentId);
                logger.debug("Deleted results for student id={}", studentId);

                studentCourseRepository.deleteAllByStudentId(studentId);
                logger.debug("Deleted student courses for student id={}", studentId);

                feeRepository.deleteByStudentId(studentId);
                logger.debug("Deleted fees for student id={}", studentId);

                // Finally delete the student record
                studentRepository.delete(student);
                logger.info("Student id={} deleted", studentId);

            } catch (Exception e) {
                logger.error("Error during cascade delete for student id={}: {}", studentId, e.getMessage());
                throw new RuntimeException("Failed to delete student records: " + e.getMessage(), e);
            }
        }

        // 2. Handle COLLEGE user
        College college = collegeRepository.findByUserId(userId).orElse(null);
        if (college != null) {
            logger.info("Deleting college id={}", college.getId());
            collegeRepository.delete(college);
            logger.info("College id={} deleted", college.getId());
        }

        // 3. Finally delete the user
        userRepository.delete(user);
        logger.info("User id={} deleted successfully", userId);
    }
}
