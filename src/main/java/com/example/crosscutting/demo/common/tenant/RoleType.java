package com.example.crosscutting.demo.common.tenant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RoleType {
    SYSTEM_ADMIN("system_admin"),
    TENANT_ADMIN("tenant_admin");

    private final String type;
    RoleType(String type){
        this.type = type;
    }
    public static final String SYSTEM_ADMIN_TYPE = SYSTEM_ADMIN.type;
    public static final String TENANT_ADMIN_TYPE = TENANT_ADMIN.type;

    @JsonValue
    public String getType() {
        return type;
    }

    @JsonCreator
    public static RoleType fromType(String type) {
        for (RoleType role : values()) {
            if (role.type.equalsIgnoreCase(type)) {
                return role;
            }
        }
        return null;
    }
}
