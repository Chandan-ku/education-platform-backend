package com.collegenet.collegenet.service;

import com.collegenet.collegenet.entity.College;
import com.collegenet.collegenet.repo.CollegeRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollegeService {
    private final CollegeRepo collegeRepository;

    public CollegeService(CollegeRepo collegeRepository) {
        this.collegeRepository = collegeRepository;
    }

    public College createCollege(College college) {
        return collegeRepository.save(college);
    }

    public List<College> getAllColleges() {
        return collegeRepository.findAll();
    }

    public College getCollegeById(Long id) {
        return collegeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("College not found"));
    }

    public College approveCollege(Long id) {
        College college = getCollegeById(id);
        college.setApproved(true);
        return collegeRepository.save(college);
    }
}
