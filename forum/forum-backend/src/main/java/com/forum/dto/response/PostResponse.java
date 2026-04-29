package com.forum.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String summary;        // 内容摘要
    private String authorName;
    private Long authorId;
    private String categoryName;
    private Long categoryId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer isTop;
    private Integer isEssence;
    private LocalDateTime createTime;
    private LocalDateTime lastCommentTime;
}