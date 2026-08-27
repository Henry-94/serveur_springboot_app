package com.reseausocial.group.service;

import com.reseausocial.group.entity.User;
import com.reseausocial.group.exception.InvalidCredentialsException;
import com.reseausocial.group.repository.UserRepository;
import com.reseausocial.group.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceResetPasswordTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldResetPasswordWhenEmailExistsAndPasswordsMatch() {
        User user = User.builder()
                .id(1L)
                .email("alice@ecole.fr")
                .password("old-password")
                .fullName("Alice")
                .studentMatricule("E123")
                .build();

        when(userRepository.findByEmail("alice@ecole.fr")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-secret-123")).thenReturn("encoded-new-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.resetPassword("alice@ecole.fr", "new-secret-123", "new-secret-123");

        assertNotNull(result);
        assertEquals("encoded-new-password", result.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@ecole.fr")).thenReturn(Optional.empty());

        InvalidCredentialsException ex = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.resetPassword("missing@ecole.fr", "new-secret-123", "new-secret-123")
        );

        assertNotNull(ex);
    }
}
