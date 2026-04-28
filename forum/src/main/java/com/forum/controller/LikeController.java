package com.forum.controller;

import com.forum.annotation.LoginRequired;
import com.forum.common.result.Result;
import com.forum.dto.response.LikeStatusResponse;
import com.forum.service.LikeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * 点赞
     */
    @PostMapping("/{targetType}/{targetId}")
    @LoginRequired
    public Result<Void> like(@PathVariable String targetType,
                             @PathVariable Long targetId,
                             HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        likeService.like(userId, targetType, targetId);
        return Result.success("点赞成功", null);
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/{targetType}/{targetId}")
    @LoginRequired
    public Result<Void> unlike(@PathVariable String targetType,
                               @PathVariable Long targetId,
                               HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        likeService.unlike(userId, targetType, targetId);
        return Result.success("取消点赞成功", null);
    }

    /**
     * 获取点赞状态和数量
     */
    @GetMapping("/status/{targetType}/{targetId}")
    @LoginRequired
    public Result<LikeStatusResponse> getLikeStatus(@PathVariable String targetType,
                                                    @PathVariable Long targetId,
                                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        boolean isLiked = likeService.getLikeStatus(userId, targetType, targetId);
        Integer likeCount = likeService.getLikeCount(targetType, targetId);

        LikeStatusResponse response = LikeStatusResponse.builder()
                .isLiked(isLiked)
                .likeCount(likeCount)
                .build();

        return Result.success(response);
    }
}