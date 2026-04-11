package com.collegenet.collegenet.repo;

import com.collegenet.collegenet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User,Long> {
    User findByEmail(String email);
}
