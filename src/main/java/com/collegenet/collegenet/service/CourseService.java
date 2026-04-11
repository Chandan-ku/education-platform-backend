package com.collegenet.collegenet.service;

import com.collegenet.collegenet.dto.CourseDTO;
import com.collegenet.collegenet.entity.Course;
import com.collegenet.collegenet.exception.ResourceNotFoundException;
import com.collegenet.collegenet.repo.CourseRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepo courseRepository;

    /**
     * Create a new course
     */
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    /**
     * Get all courses as entities
     */
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * Get all courses as DTOs
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCoursesDTO() {
        return courseRepository.findAll()
                .stream()
                .map(this::convertToCourseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get course by ID as entity
     */
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    /**
     * Get course by ID as DTO
     */
    @Transactional(readOnly = true)
    public CourseDTO getCourseDTOById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return convertToCourseDTO(course);
    }

    /**
     * Update course
     */
    @Transactional
    public Course updateCourse(Long id, Course courseDetails) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        if (courseDetails.getTitle() != null) {
            course.setTitle(courseDetails.getTitle());
        }
        if (courseDetails.getDescription() != null) {
            course.setDescription(courseDetails.getDescription());
        }
        if (courseDetails.getPrice() != null) {
            course.setPrice(courseDetails.getPrice());
        }
        if (courseDetails.getJobGuaranteed() != null) {
            course.setJobGuaranteed(courseDetails.getJobGuaranteed());
        }
        if (courseDetails.getDurationInWeeks() != null) {
            course.setDurationInWeeks(courseDetails.getDurationInWeeks());
        }

        return courseRepository.save(course);
    }

    /**
     * Delete course
     */
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        courseRepository.delete(course);
    }

    /**
     * Get courses by job guarantee status
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByJobGuarantee(Boolean jobGuaranteed) {
        return courseRepository.findAll()
                .stream()
                .filter(c -> c.getJobGuaranteed() != null && c.getJobGuaranteed().equals(jobGuaranteed))
                .map(this::convertToCourseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get courses sorted by price
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesSortedByPrice(boolean ascending) {
        List<Course> courses = courseRepository.findAll();
        if (ascending) {
            return courses.stream()
                    .sorted((c1, c2) -> (c1.getPrice() != null && c2.getPrice() != null) ? c1.getPrice().compareTo(c2.getPrice()) : 0)
                    .map(this::convertToCourseDTO)
                    .collect(Collectors.toList());
        } else {
            return courses.stream()
                    .sorted((c1, c2) -> (c1.getPrice() != null && c2.getPrice() != null) ? c2.getPrice().compareTo(c1.getPrice()) : 0)
                    .map(this::convertToCourseDTO)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Get courses by duration range
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByDurationRange(Integer minWeeks, Integer maxWeeks) {
        return courseRepository.findAll()
                .stream()
                .filter(c -> c.getDurationInWeeks() != null
                        && c.getDurationInWeeks() >= minWeeks
                        && c.getDurationInWeeks() <= maxWeeks)
                .map(this::convertToCourseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search courses by title
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> searchCoursesByTitle(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return courseRepository.findAll()
                .stream()
                .filter(c -> c.getTitle() != null && c.getTitle().toLowerCase().contains(lowerKeyword))
                .map(this::convertToCourseDTO)
                .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    private CourseDTO convertToCourseDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setPrice(course.getPrice());
        dto.setJobGuaranteed(course.getJobGuaranteed());
        dto.setDurationInWeeks(course.getDurationInWeeks());
        return dto;
    }
}
