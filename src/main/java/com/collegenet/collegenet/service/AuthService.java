package com.collegenet.collegenet.service;

import com.collegenet.collegenet.dto.AuthResponse;
import com.collegenet.collegenet.dto.LoginRequest;
import com.collegenet.collegenet.dto.RegisterRequest;
import com.collegenet.collegenet.entity.User;
import com.collegenet.collegenet.entity.Role;
import com.collegenet.collegenet.entity.Student;
import com.collegenet.collegenet.exception.AuthenticationException;
import com.collegenet.collegenet.exception.DuplicateEmailException;
import com.collegenet.collegenet.repo.UserRepo;
import com.collegenet.collegenet.repo.StudentRepo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepo userRepo;
    private final StudentRepo studentRepo;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepo userRepo, StudentRepo studentRepo) {
        this.userRepo = userRepo;
        this.studentRepo = studentRepo;
    }

    public AuthResponse register(RegisterRequest request) {
        // check if email exists
        if (userRepo.findByEmail(request.getEmail()) != null) {
            throw new DuplicateEmailException("Email is already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // default role as STUDENT
        user.setRole(Role.STUDENT);
        user.setFullName(request.getUsername());

        User saved = userRepo.save(user);
        logger.info("User created: id={}, email={}, role={}", saved.getId(), saved.getEmail(), saved.getRole());

        // NOTE: Automatic creation of Student records was removed because some deployments
        // enforce NOT NULL on students.college_id. Creating a Student record requires
        // additional context (college assignment) and should be done via the Student API.
        Long studentId = null;

        /*
        // Previous behavior (auto-create Student) removed to avoid failing DB inserts.
        try {
            Student student = new Student();
            student.setUser(saved);
            student.setEnrollmentNumber(generateEnrollmentNumber());
            student.setFeesPaid(false);

            Student savedStudent = studentRepo.save(student);
            studentId = savedStudent.getId();
            logger.info("Student record auto-created: id={}, enrollmentNumber={}",
                       savedStudent.getId(), savedStudent.getEnrollmentNumber());
        } catch (Exception e) {
            logger.warn("Auto-creation of Student record skipped for user id={}: {}",
                       saved.getId(), e.getMessage());
            // Continue - front-end or integration should create Student via /api/students
        }
        */

        return new AuthResponse(saved.getId(), studentId, saved.getEmail(), "User registered successfully. If this is a student account, create the Student profile using POST /api/students (StudentRequestDTO).");
    }

    public AuthResponse login(LoginRequest request) {
        // try find by email first
        User user = userRepo.findByEmail(request.getUsernameOrEmail());
        if (user == null) {
            throw new AuthenticationException("Invalid email or password");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        Long studentId = null;

        // NOTE: Auto-creation of Student has been disabled for safety.
        // If a Student entity is required, create it via the StudentService (/api/students).
        try {
            Student student = studentRepo.findByUserId(user.getId()).orElse(null);
            if (student != null) {
                studentId = student.getId();
            } else {
                logger.info("No Student record found for user id={}. To create one, call POST /api/students.", user.getId());
            }
        } catch (Exception e) {
            logger.warn("Unable to look up Student record for user id={}: {}", user.getId(), e.getMessage());
        }

        return new AuthResponse(user.getId(), studentId, user.getEmail(), "Login successful");
    }

}
