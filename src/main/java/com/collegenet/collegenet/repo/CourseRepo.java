package com.collegenet.collegenet.repo;

import com.collegenet.collegenet.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepo extends JpaRepository<Course, Long> {

}
