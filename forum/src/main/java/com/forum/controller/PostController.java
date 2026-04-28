package com.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.annotation.LoginRequired;
import com.forum.common.result.Result;
import com.forum.dto.request.PostCreateRequest;
import com.forum.dto.request.PostSearchRequest;
import com.forum.dto.request.PostUpdateRequest;
import com.forum.dto.response.CategoryResponse;
import com.forum.dto.response.PostDetailResponse;
import com.forum.dto.response.PostResponse;
import com.forum.service.PostService;
import com.forum.entity.User;
import com.forum.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserMapper userMapper;

    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("PostController is working!");
    }

    /**
     * 发布帖子
     */
    @PostMapping("/create")
    @LoginRequired
    public Result<Long> createPost(@Valid @RequestBody PostCreateRequest request,
                                   HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Long postId = postService.createPost(userId, request);
        return Result.success("发布成功", postId);
    }

    /**
     * 更新帖子
     */
    @PutMapping("/update/{postId}")
    @LoginRequired
    public Result<Void> updatePost(@PathVariable Long postId,
                                   @Valid @RequestBody PostUpdateRequest request,
                                   HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        postService.updatePost(userId, postId, request);
        return Result.success("更新成功", null);
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/delete/{postId}")
    @LoginRequired
    public Result<Void> deletePost(@PathVariable Long postId,
                                   HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        postService.deletePost(userId, postId);
        return Result.success("删除成功", null);
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{postId}")
    public Result<PostDetailResponse> getPostDetail(@PathVariable Long postId,
                                                    HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("userId");
        PostDetailResponse detail = postService.getPostDetail(postId, currentUserId);
        return Result.success(detail);
    }

    /**
     * 获取帖子列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<PostResponse>> getPostList(@ModelAttribute PostSearchRequest request) {
        Page<PostResponse> page = postService.getPostList(request);
        return Result.success(page);
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public Result<List<CategoryResponse>> getCategories() {
        List<CategoryResponse> categories = postService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * 置顶帖子（管理员/版主）
     */
    @PutMapping("/top/{postId}")
    @LoginRequired
    public Result<Void> topPost(@PathVariable Long postId,
                                @RequestParam Boolean isTop,
                                HttpServletRequest httpRequest) {
        // 检查权限
        Long userId = (Long) httpRequest.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (!"admin".equals(user.getRole()) && !"moderator".equals(user.getRole())) {
            return Result.error("没有权限执行此操作");
        }

        postService.topPost(postId, isTop);
        return Result.success(isTop ? "置顶成功" : "取消置顶成功", null);
    }

    /**
     * 加精帖子（管理员/版主）
     */
    @PutMapping("/essence/{postId}")
    @LoginRequired
    public Result<Void> essencePost(@PathVariable Long postId,
                                    @RequestParam Boolean isEssence,
                                    HttpServletRequest httpRequest) {
        // 检查权限
        Long userId = (Long) httpRequest.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (!"admin".equals(user.getRole()) && !"moderator".equals(user.getRole())) {
            return Result.error("没有权限执行此操作");
        }

        postService.essencePost(postId, isEssence);
        return Result.success(isEssence ? "加精成功" : "取消加精成功", null);
    }
}