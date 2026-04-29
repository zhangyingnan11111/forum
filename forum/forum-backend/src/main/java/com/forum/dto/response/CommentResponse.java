package com.forum.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private Long postId;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private Integer likeCount;
    private Boolean isLiked;
    private LocalDateTime createTime;
}