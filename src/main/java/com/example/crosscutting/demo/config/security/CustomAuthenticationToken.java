package com.example.crosscutting.demo.config.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;


public class CustomAuthenticationToken extends AbstractAuthenticationToken {
    private final String principal;
    private final String id;
    private final String credentials;
    private final Tenant tenant;

    public CustomAuthenticationToken(String principal,
                                     String id,
                                     String credentials,
                                     Tenant tenant,
                                     Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.id = id;
        this.credentials = credentials;
        this.tenant = tenant;
        setAuthenticated(true);
    }

    public String getId() {
        return id;
    }

    @Override
    public String getCredentials() {
        return this.credentials;
    }

    @Override
    public String getPrincipal() {
        return this.principal;
    }

    public Tenant getTenant() {
        return tenant;
    }
}