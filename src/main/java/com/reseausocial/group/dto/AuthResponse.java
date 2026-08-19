package com.reseausocial.group.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    @Default
    private String tokenType = "Bearer";
    private UserResponse user;

    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.tokenType = "Bearer";
        this.user = user;
    }
}
