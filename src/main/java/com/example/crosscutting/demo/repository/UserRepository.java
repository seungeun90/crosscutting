package com.example.crosscutting.demo.repository;


import com.example.crosscutting.demo.repository.dao.UserDao;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository {
    void saveUser(UserDao userDao);
}
