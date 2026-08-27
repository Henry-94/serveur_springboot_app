package com.reseausocial.group.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 80) String mention,
        @NotBlank @Size(max = 100) String parcours,
        @NotBlank @Size(max = 20) String niveau
) {}
