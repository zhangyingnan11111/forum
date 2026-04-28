package com.forum.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String type;
    private String senderName;
    private Long senderId;
    private String senderAvatar;
    private String targetType;
    private Long targetId;
    private String content;
    private Boolean isRead;
    private LocalDateTime createTime;
}