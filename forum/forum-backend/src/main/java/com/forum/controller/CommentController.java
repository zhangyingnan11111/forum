package com.forum.controller;

import com.forum.annotation.LoginRequired;
import com.forum.common.result.Result;
import com.forum.dto.request.CommentCreateRequest;
import com.forum.dto.response.CommentResponse;
import com.forum.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 发表评论
     */
    @PostMapping("/create")
    @LoginRequired
    public Result<Long> createComment(@Valid @RequestBody CommentCreateRequest request,
                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Long commentId = commentService.createComment(userId, request);
        return Result.success("评论成功", commentId);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{commentId}")
    @LoginRequired
    public Result<Void> deleteComment(@PathVariable Long commentId,
                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        commentService.deleteComment(userId, commentId);
        return Result.success("删除成功", null);
    }

    /**
     * 获取帖子的评论列表
     */
    @GetMapping("/list/{postId}")
    public Result<List<CommentResponse>> getPostComments(@PathVariable Long postId,
                                                         HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("userId");
        List<CommentResponse> comments = commentService.getPostComments(postId, currentUserId);
        return Result.success(comments);
    }
}