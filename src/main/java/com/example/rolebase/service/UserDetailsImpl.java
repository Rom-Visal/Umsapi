package com.example.rolebase.service;

import com.example.rolebase.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public record UserDetailsImpl(User user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Maps roles to granted authorities with a prefix
        return user().getRoles().stream()
                .map(userRole -> new SimpleGrantedAuthority(
                        "ROLE_" + userRole.getRole().getName()))
                .toList();
    }

    @Override
    public String getPassword() {
        return user().getPassword();
    }

    @Override
    public String getUsername() {
        return user().getUsername();
    }
}
