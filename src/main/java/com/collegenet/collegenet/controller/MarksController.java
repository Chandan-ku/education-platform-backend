package com.collegenet.collegenet.controller;

import com.collegenet.collegenet.entity.Marks;
import com.collegenet.collegenet.service.MarksService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marks")
@RequiredArgsConstructor
public class MarksController {

    private final MarksService marksService;

    @GetMapping
    public List<Marks> getAllMarks() {
        return marksService.getAllMarks();
    }

    @PostMapping
    public Marks addMarks(@RequestBody Marks marks) {
        return marksService.addMarks(marks);
    }

    @GetMapping("/{studentId}/{semester}")
    public List<Marks> getMarks(
            @PathVariable Long studentId,
            @PathVariable Integer semester) {

        return marksService.getMarks(studentId, semester);
    }
}

