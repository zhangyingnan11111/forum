package com.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.annotation.LoginRequired;
import com.forum.annotation.RequireRole;
import com.forum.common.result.Result;
import com.forum.dto.response.PostResponse;
import com.forum.dto.response.UserInfoResponse;
import com.forum.entity.User;
import com.forum.mapper.UserMapper;
import com.forum.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@LoginRequired
@RequireRole({"admin"})  // 只有管理员可以访问
public class AdminController {

    private final UserMapper userMapper;
    private final PostService postService;

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public Result<Page<UserInfoResponse>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }

        wrapper.orderByDesc(User::getCreateTime);

        Page<User> userPage = userMapper.selectPage(page, wrapper);

        Page<UserInfoResponse> responsePage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserInfoResponse> records = userPage.getRecords().stream()
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());
        responsePage.setRecords(records);

        return Result.success(responsePage);
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/user/{userId}/role")
    public Result<Void> updateUserRole(@PathVariable Long userId,
                                       @RequestParam String role) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 不允许修改自己的角色
        // 实际项目中应该获取当前用户ID进行判断

        user.setRole(role);
        userMapper.updateById(user);

        log.info("更新用户角色: userId={}, role={}", userId, role);
        return Result.success("更新成功", null);
    }

    /**
     * 禁用/启用用户
     */
    @PutMapping("/user/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Long userId,
                                         @RequestParam Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setStatus(status);
        userMapper.updateById(user);

        log.info("更新用户状态: userId={}, status={}", userId, status);
        return Result.success("更新成功", null);
    }

    /**
     * 获取所有帖子（管理视角）
     */
    @GetMapping("/posts")
    public Result<Page<PostResponse>> getPostList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        com.forum.dto.request.PostSearchRequest request = new com.forum.dto.request.PostSearchRequest();
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        request.setKeyword(keyword);

        // 管理员可以查看所有状态的帖子，不需要过滤status=1
        Page<PostResponse> page = postService.getPostList(request);

        // 如果指定了状态，需要额外过滤（简化实现，实际应该在service层处理）
        if (status != null) {
            List<PostResponse> filteredRecords = page.getRecords().stream()
                    .filter(post -> {
                        // 这里需要根据实际需求过滤
                        return true;
                    })
                    .collect(Collectors.toList());
            page.setRecords(filteredRecords);
        }

        return Result.success(page);
    }

    /**
     * 管理员删除任意帖子
     */
    @DeleteMapping("/post/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId) {
        postService.adminDeletePost(postId);
        return Result.success("删除成功", null);
    }

    /**
     * 批量删除帖子
     */
    @DeleteMapping("/posts/batch")
    public Result<Void> batchDeletePosts(@RequestBody List<Long> postIds) {
        postService.batchDeletePosts(postIds);
        return Result.success("批量删除成功", null);
    }

    /**
     * 获取系统统计信息
     */
    @GetMapping("/statistics")
    public Result<StatisticsResponse> getStatistics() {
        // 统计用户数
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<>());

        // 统计帖子数
        Long postCount = postService.getPostCount();

        // 统计今日新增等（简化实现）

        StatisticsResponse statistics = StatisticsResponse.builder()
                .userCount(userCount)
                .postCount(postCount)
                .build();

        return Result.success(statistics);
    }

    private UserInfoResponse convertToUserInfo(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .intro(user.getIntro())
                .role(user.getRole())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .build();
    }
}

// 统计响应类
class StatisticsResponse {
    private Long userCount;
    private Long postCount;
    private Long commentCount;
    private Long todayNewUsers;
    private Long todayNewPosts;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private StatisticsResponse response = new StatisticsResponse();

        public Builder userCount(Long userCount) {
            response.userCount = userCount;
            return this;
        }

        public Builder postCount(Long postCount) {
            response.postCount = postCount;
            return this;
        }

        public StatisticsResponse build() {
            return response;
        }
    }

    // Getters and Setters
    public Long getUserCount() { return userCount; }
    public void setUserCount(Long userCount) { this.userCount = userCount; }
    public Long getPostCount() { return postCount; }
    public void setPostCount(Long postCount) { this.postCount = postCount; }
}