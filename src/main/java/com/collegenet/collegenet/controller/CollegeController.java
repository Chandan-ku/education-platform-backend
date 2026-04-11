package com.collegenet.collegenet.controller;

import com.collegenet.collegenet.entity.College;
import com.collegenet.collegenet.service.CollegeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colleges")
public class CollegeController {

    private final CollegeService collegeService;

    public CollegeController(CollegeService collegeService) {
        this.collegeService = collegeService;
    }

    // College Registration
    @PostMapping
    public College registerCollege(@RequestBody College college) {
        return collegeService.createCollege(college);
    }

    // Admin / Public view
    @GetMapping
    public List<College> getAllColleges() {
        return collegeService.getAllColleges();
    }

    // Get college by ID
    @GetMapping("/{id}")
    public College getCollege(@PathVariable Long id) {
        return collegeService.getCollegeById(id);
    }

    // Admin approval
    @PutMapping("/{id}/approve")
    public College approveCollege(@PathVariable Long id) {
        return collegeService.approveCollege(id);
    }
}
