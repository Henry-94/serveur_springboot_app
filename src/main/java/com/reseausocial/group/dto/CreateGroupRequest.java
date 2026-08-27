package com.reseausocial.group.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record CreateGroupRequest(@NotBlank @Size(max=120) String name, @Size(max=500) String description) {}
