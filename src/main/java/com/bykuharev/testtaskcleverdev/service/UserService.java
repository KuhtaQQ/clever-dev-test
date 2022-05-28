package com.bykuharev.testtaskcleverdev.service;

import com.bykuharev.testtaskcleverdev.dao.UserRepository;
import com.bykuharev.testtaskcleverdev.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@Slf4j

public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByLoginOrCreate(String login) {
        User userLogin = userRepository.findByLogin(login);
        if (userLogin==null) {
            return create(login);
        }
        return userLogin;
    }



    private User create(String login) {
        try {
            log.info("User with login: {} created", login);
            return userRepository.save(new User(login));
        } catch (HibernateException e) {
            log.error("Error to create user with login: {}", login);
            return null;
        }
    }
}
