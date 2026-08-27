package com.reseausocial.group.controller;

import com.reseausocial.group.dto.AuthResponse;
import com.reseausocial.group.dto.LoginRequest;
import com.reseausocial.group.dto.RegisterRequest;
import com.reseausocial.group.dto.ResetPasswordRequest;
import com.reseausocial.group.dto.UserResponse;
import com.reseausocial.group.dto.UpdateProfileRequest;
import com.reseausocial.group.entity.User;
import com.reseausocial.group.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller - Auth (Creer un compte / Se connecter / Consulter profil)
 * Cf. Diagramme de Cas d'Utilisation du document de conception.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register  -> écran "Créer un compte"
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/auth/login  -> écran "Se connecter"
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // GET /api/auth/me  -> utilisateur courant (JWT requis) -> écran "Profil"
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/me", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<AuthResponse> updateMe(@AuthenticationPrincipal UserDetails userDetails,
                                                  @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(userDetails.getUsername(), request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Les mots de passe ne correspondent pas.");
        }

        User user = authService.resetPassword(request.getEmail(), request.getNewPassword(), request.getConfirmPassword());
        return ResponseEntity.ok("Mot de passe réinitialisé avec succès pour " + user.getEmail());
    }
}
