package com.example.rolebase.service;

import com.example.rolebase.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void updateUserStatus_updatesEnabledFlag() {
        String username = "john";
        boolean enabled = true;
        when(userRepository.updateUserEnabledStatus(username, enabled)).thenReturn(1);

        userService.updateUserStatus(username, enabled);

        verify(userRepository).updateUserEnabledStatus(username, enabled);
    }
}
