package com.forum.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String intro;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
}