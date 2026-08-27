package com.reseausocial.group.dto;

import jakarta.validation.constraints.NotBlank;

public record ReactionRequest(@NotBlank String emoji) {}
