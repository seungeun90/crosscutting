package com.example.crosscutting.demo.repository;


import com.example.crosscutting.demo.common.db.Schema;
import com.example.crosscutting.demo.common.db.Table;
import com.example.crosscutting.demo.repository.dao.UserDao;
import org.springframework.stereotype.Repository;


@Repository
@Table(schema= Schema.GLOBAL)
public interface UserRepository {
    void saveUser(UserDao userDao);
}
