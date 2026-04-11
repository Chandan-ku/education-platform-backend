package com.collegenet.collegenet.service;

import com.collegenet.collegenet.dto.StudentRequestDTO;
import com.collegenet.collegenet.dto.StudentResponseDTO;
import com.collegenet.collegenet.dto.StudentDashboardResponse;
import com.collegenet.collegenet.entity.Student;
import com.collegenet.collegenet.entity.Result;
import com.collegenet.collegenet.repo.StudentRepo;
import com.collegenet.collegenet.repo.ResultRepo;
import com.collegenet.collegenet.repo.FeeRepo;
import com.collegenet.collegenet.repo.PaymentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepo studentRepository;
    private final ResultRepo resultRepository;
    private final FeeRepo feeRepository;
    private final PaymentRepo paymentRepository;

    public StudentResponseDTO createStudent(StudentRequestDTO dto) {

        Student student = Student.builder()
                .enrollmentNumber(dto.getEnrollmentNumber())
                .course(dto.getCourse())
                .semester(dto.getSemester())
                .attendancePercentage(0.0)
                .feesPaid(false)
                .build();

        Student saved = studentRepository.save(student);

        return StudentResponseDTO.builder()
                .id(saved.getId())
                .enrollmentNumber(saved.getEnrollmentNumber())
                .course(saved.getCourse())
                .semester(saved.getSemester())
                .attendancePercentage(saved.getAttendancePercentage())
                .feesPaid(saved.getFeesPaid())
                .build();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getByEnrollmentNumber(String enrollmentNumber) {
        return studentRepository.findByEnrollmentNumber(enrollmentNumber)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    public StudentDashboardResponse getStudentDashboard(Long studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Fetch optional related data
        Result latestResult = resultRepository.findTopByStudentIdOrderByIdDesc(studentId);
        Double paidFee = paymentRepository.sumPaidAmountByStudentId(studentId);
        Double totalFee = feeRepository.findByStudentId(studentId)
                .map(fee -> fee.getTotalFee())
                .orElse(0.0);

        // Build response with proper null checks
        StudentDashboardResponse response = new StudentDashboardResponse();
        response.setStudentId(student.getId());

        // ✅ Student name comes from User (with fallback)
        String studentName = (student.getUser() != null && student.getUser().getFullName() != null)
            ? student.getUser().getFullName()
            : "Student " + student.getId();
        response.setStudentName(studentName);

        // ✅ College name (with fallback)
        String collegeName = (student.getCollege() != null && student.getCollege().getCollegeName() != null)
            ? student.getCollege().getCollegeName()
            : "Unknown College";
        response.setCollegeName(collegeName);

        // ✅ Course is String (with fallback)
        String courseName = (student.getCourse() != null && !student.getCourse().isEmpty())
            ? student.getCourse()
            : "Not Assigned";
        response.setCourseName(courseName);

        // ✅ Semester is Integer (with fallback)
        String currentSemester = (student.getSemester() != null)
            ? "Semester " + student.getSemester()
            : "Semester Not Assigned";
        response.setCurrentSemester(currentSemester);

        // ✅ Result data (optional)
        if (latestResult != null) {
            response.setCgpa(latestResult.getCgpa() != null ? latestResult.getCgpa() : 0.0);
            response.setResultStatus(latestResult.getStatus() != null ? latestResult.getStatus() : "Pending");
        } else {
            response.setCgpa(0.0);
            response.setResultStatus("No Results Yet");
        }

        // ✅ Fee calculation
        response.setTotalFee(totalFee != null ? totalFee : 0.0);
        response.setPaidFee(paidFee != null ? paidFee : 0.0);
        response.setDueFee(response.getTotalFee() - response.getPaidFee());

        return response;
    }


}
