package com.reseausocial.group.dto;

import com.reseausocial.group.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String studentMatricule;
    private String mention;
    private String parcours;
    private String niveau;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .studentMatricule(user.getStudentMatricule())
                .mention(user.getMention())
                .parcours(user.getParcours())
                .niveau(user.getNiveau())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
