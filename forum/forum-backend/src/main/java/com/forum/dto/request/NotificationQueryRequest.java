package com.forum.dto.request;

import lombok.Data;

@Data
public class NotificationQueryRequest {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private Boolean unreadOnly = false;
}