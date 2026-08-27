package com.reseausocial.group.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
public record CreateEventRequest(@NotBlank String title, String description, @NotNull LocalDateTime startsAt, String location, String category, String image) {}
