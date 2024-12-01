package com.example.crosscutting.demo.config.security;

import lombok.Builder;

@Builder
public record Tenant(
        String id,
        String name,
        String code
){
}
