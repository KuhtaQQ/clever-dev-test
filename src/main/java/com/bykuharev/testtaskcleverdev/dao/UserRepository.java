package com.bykuharev.testtaskcleverdev.dao;

import com.bykuharev.testtaskcleverdev.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByLogin(String login);
}
