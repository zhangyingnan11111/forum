package com.forum.dto.request;

import lombok.Data;

@Data
public class PostSearchRequest {

    private String keyword;      // 搜索关键词
    private Long categoryId;     // 分类ID
    private Long authorId;       // 作者ID
    private String sortBy;       // 排序字段: createTime, viewCount, commentCount
    private String order;        // 排序方向: ASC, DESC
    private Integer pageNum = 1; // 页码
    private Integer pageSize = 10; // 每页大小
}