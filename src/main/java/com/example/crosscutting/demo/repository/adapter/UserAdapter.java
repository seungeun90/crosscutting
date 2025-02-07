package com.example.crosscutting.demo.repository.adapter;

import com.example.crosscutting.demo.common.tenant.SetTenant;
import com.example.crosscutting.demo.repository.UserRepository;
import com.example.crosscutting.demo.repository.dao.UserDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserAdapter {
    private final UserRepository userRepository;


    public int count(){
        return 0;
    }

    @SetTenant
    public UserDao getUser(){
        return null;
    }
}
