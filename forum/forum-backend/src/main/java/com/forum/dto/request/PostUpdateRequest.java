package com.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostUpdateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 200, message = "标题长度必须在1-200之间")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Long categoryId;
}