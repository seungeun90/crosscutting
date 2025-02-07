package com.example.crosscutting.demo.common.tenant;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface SetTenant {
}
