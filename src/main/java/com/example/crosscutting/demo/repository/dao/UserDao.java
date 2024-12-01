package com.example.crosscutting.demo.repository.dao;

import com.example.crosscutting.demo.common.db.Secured;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class UserDao {
    private String id;

    @Secured
    private String email;
}
