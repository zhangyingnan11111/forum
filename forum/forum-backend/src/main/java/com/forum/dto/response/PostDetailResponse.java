package com.forum.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private Long authorId;
    private String authorAvatar;
    private String categoryName;
    private Long categoryId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer isTop;
    private Integer isEssence;
    private Boolean isLiked;       // 当前用户是否点赞
    private Boolean isCollected;   // 当前用户是否收藏
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}