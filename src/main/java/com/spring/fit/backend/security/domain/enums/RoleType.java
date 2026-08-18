package com.spring.fit.backend.security.domain.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    USER("USER"),
    ADMIN("ADMIN"),
    STAFF("STAFF");

    private final String roleName;

    RoleType(String roleName) {
        this.roleName = roleName;
    }

    public String getAuthority() {
        return "ROLE_" + roleName;
    }
}
