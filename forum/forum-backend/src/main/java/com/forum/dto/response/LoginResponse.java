package com.forum.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private Long userId;
    private String username;
    private String email;
    private String avatar;
    private String role;
    private String token;
    private Long expiresIn;
}