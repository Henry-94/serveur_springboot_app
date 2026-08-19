package com.reseausocial.group.service;

import com.reseausocial.group.dto.AuthResponse;
import com.reseausocial.group.dto.LoginRequest;
import com.reseausocial.group.dto.RegisterRequest;
import com.reseausocial.group.dto.UserResponse;
import com.reseausocial.group.dto.UpdateProfileRequest;
import com.reseausocial.group.entity.User;
import com.reseausocial.group.exception.EmailAlreadyUsedException;
import com.reseausocial.group.exception.InvalidCredentialsException;
import com.reseausocial.group.exception.StudentMatriculeAlreadyUsedException;
import com.reseausocial.group.repository.UserRepository;
import com.reseausocial.group.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Service - Logique metier de l'authentification
 * (Controller -> Service -> Repository, cf. Architecture en couches du PDF)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException(request.getEmail());
        }
        if (userRepository.existsByStudentMatricule(request.getStudentMatricule())) {
            throw new StudentMatriculeAlreadyUsedException(request.getStudentMatricule());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .studentMatricule(request.getStudentMatricule())
                .mention(request.getMention())
                .parcours(request.getParcours())
                .niveau(request.getNiveau())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser);

        return new AuthResponse(token, UserResponse.fromEntity(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(InvalidCredentialsException::new);

        String token = jwtUtil.generateToken(user);

        return new AuthResponse(token, UserResponse.fromEntity(user));
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public AuthResponse updateProfile(String currentEmail, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(currentEmail.toLowerCase().trim())
                .orElseThrow(InvalidCredentialsException::new);
        String email = request.email().toLowerCase().trim();
        if (userRepository.existsByEmailAndIdNot(email, user.getId())) {
            throw new EmailAlreadyUsedException(email);
        }
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setMention(request.mention().trim());
        user.setParcours(request.parcours().trim());
        user.setNiveau(request.niveau().trim());
        User saved = userRepository.save(user);
        return new AuthResponse(jwtUtil.generateToken(saved), UserResponse.fromEntity(saved));
    }

    @Transactional
    public User resetPassword(String email, String newPassword, String confirmPassword) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(InvalidCredentialsException::new);

        if (!Objects.equals(newPassword, confirmPassword)) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
}
