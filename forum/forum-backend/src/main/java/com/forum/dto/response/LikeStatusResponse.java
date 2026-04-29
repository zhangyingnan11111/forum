package com.forum.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LikeStatusResponse {
    private Boolean isLiked;
    private Integer likeCount;
}