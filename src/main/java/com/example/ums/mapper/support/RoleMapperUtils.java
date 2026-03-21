package com.example.ums.mapper.support;

import com.example.ums.entity.UserRole;
import org.springframework.stereotype.Component;

@Component
public class RoleMapperUtils {

    public String fromRole(UserRole userRole) {
        if (userRole == null) {
            return null;
        }
        return userRole.getRole().getName();
    }
}
