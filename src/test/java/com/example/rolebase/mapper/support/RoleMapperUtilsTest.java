package com.example.rolebase.mapper.support;

import com.example.rolebase.entity.Role;
import com.example.rolebase.entity.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RoleMapperUtilsTest {

    private final RoleMapperUtils roleMapperUtils = new RoleMapperUtils();

    @Test
    void fromRole_returnsNullWhenInputIsNull() {
        assertNull(roleMapperUtils.fromRole(null));
    }

    @Test
    void fromRole_returnsRoleName() {
        Role role = new Role();
        role.setName("ADMIN");
        UserRole userRole = new UserRole();
        userRole.setRole(role);

        assertEquals("ADMIN", roleMapperUtils.fromRole(userRole));
    }
}
