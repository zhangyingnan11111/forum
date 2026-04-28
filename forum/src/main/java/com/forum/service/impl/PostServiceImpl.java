package com.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.common.exception.BusinessException;
import com.forum.common.exception.ErrorCode;
import com.forum.dto.request.PostCreateRequest;
import com.forum.dto.request.PostSearchRequest;
import com.forum.dto.request.PostUpdateRequest;
import com.forum.dto.response.CategoryResponse;
import com.forum.dto.response.PostDetailResponse;
import com.forum.dto.response.PostResponse;
import com.forum.entity.Category;
import com.forum.entity.Post;
import com.forum.entity.User;
import com.forum.mapper.CategoryMapper;
import com.forum.mapper.PostMapper;
import com.forum.mapper.UserMapper;
import com.forum.service.PostService;
import com.forum.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final RedisUtil redisUtil;

    private static final String POST_CACHE_PREFIX = "post:detail:";
    private static final String USER_POST_COUNT_PREFIX = "user:post:count:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(Long userId, PostCreateRequest request) {
        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证分类是否存在
        Category category = categoryMapper.selectById(request.getCategoryId());
        if (category == null || category.getStatus() == 0) {
            throw new BusinessException("分类不存在");
        }

        // 创建帖子
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthorId(userId);
        post.setCategoryId(request.getCategoryId());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setStatus(1);

        int result = postMapper.insert(post);
        if (result <= 0) {
            throw new BusinessException("发布失败，请稍后重试");
        }

        // 更新用户发帖数缓存
        String cacheKey = USER_POST_COUNT_PREFIX + userId;
        redisUtil.delete(cacheKey);

        log.info("帖子发布成功: userId={}, postId={}, title={}", userId, post.getId(), post.getTitle());
        return post.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(Long userId, Long postId, PostUpdateRequest request) {
        Post post = getPostById(postId);

        // 验证权限：只有作者本人可以编辑
        if (!post.getAuthorId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION);
        }

        // 更新内容
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category == null || category.getStatus() == 0) {
                throw new BusinessException("分类不存在");
            }
            post.setCategoryId(request.getCategoryId());
        }

        postMapper.updateById(post);

        // 清除缓存
        redisUtil.delete(POST_CACHE_PREFIX + postId);

        log.info("帖子更新成功: userId={}, postId={}", userId, postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long userId, Long postId) {
        Post post = getPostById(postId);

        // 验证权限：作者本人或管理员可以删除
        User user = userMapper.selectById(userId);
        if (!post.getAuthorId().equals(userId) && !"admin".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION);
        }

        // 软删除
        post.setStatus(0);
        postMapper.updateById(post);

        // 清除缓存
        redisUtil.delete(POST_CACHE_PREFIX + postId);
        redisUtil.delete(USER_POST_COUNT_PREFIX + post.getAuthorId());

        log.info("帖子删除成功: userId={}, postId={}", userId, postId);
    }

    @Override
    public PostDetailResponse getPostDetail(Long postId, Long currentUserId) {
        // 先从缓存获取
        String cacheKey = POST_CACHE_PREFIX + postId;
        PostDetailResponse detail = (PostDetailResponse) redisUtil.get(cacheKey);

        if (detail == null) {
            Post post = getPostById(postId);
            User author = userMapper.selectById(post.getAuthorId());
            Category category = categoryMapper.selectById(post.getCategoryId());

            detail = PostDetailResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .authorName(author.getUsername())
                    .authorId(author.getId())
                    .authorAvatar(author.getAvatar())
                    .categoryName(category != null ? category.getName() : "未分类")
                    .categoryId(post.getCategoryId())
                    .viewCount(post.getViewCount())
                    .likeCount(post.getLikeCount())
                    .commentCount(post.getCommentCount())
                    .collectCount(post.getCollectCount())
                    .isTop(post.getIsTop())
                    .isEssence(post.getIsEssence())
                    .createTime(post.getCreateTime())
                    .updateTime(post.getUpdateTime())
                    .build();

            // 缓存5分钟
            redisUtil.set(cacheKey, detail, 5, TimeUnit.MINUTES);
        }

        // 增加浏览量（异步处理，不实时入库）
        postMapper.incrementViewCount(postId);

        // TODO: 查询点赞和收藏状态（需要实现点赞收藏功能时添加）
        detail.setIsLiked(false);
        detail.setIsCollected(false);

        return detail;
    }

    @Override
    public Page<PostResponse> getPostList(PostSearchRequest request) {
        Page<Post> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);

        // 关键词搜索（标题或内容）
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(Post::getTitle, request.getKeyword())
                    .or()
                    .like(Post::getContent, request.getKeyword()));
        }

        // 分类筛选
        if (request.getCategoryId() != null) {
            wrapper.eq(Post::getCategoryId, request.getCategoryId());
        }

        // 作者筛选
        if (request.getAuthorId() != null) {
            wrapper.eq(Post::getAuthorId, request.getAuthorId());
        }

        // 排序
        String sortBy = request.getSortBy();
        String order = request.getOrder();
        if (StringUtils.hasText(sortBy)) {
            boolean isAsc = "ASC".equalsIgnoreCase(order);
            switch (sortBy) {
                case "viewCount":
                    wrapper.orderBy(true, isAsc, Post::getViewCount);
                    break;
                case "commentCount":
                    wrapper.orderBy(true, isAsc, Post::getCommentCount);
                    break;
                case "createTime":
                default:
                    wrapper.orderBy(true, isAsc, Post::getCreateTime);
                    break;
            }
        } else {
            // 默认按置顶和创建时间排序
            wrapper.orderByDesc(Post::getIsTop)
                    .orderByDesc(Post::getCreateTime);
        }

        Page<Post> postPage = postMapper.selectPage(page, wrapper);

        // 转换为响应对象
        Page<PostResponse> responsePage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        List<PostResponse> records = postPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(records);

        return responsePage;
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryMapper.selectAllEnabled();
        return categories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void topPost(Long postId, boolean isTop) {
        Post post = getPostById(postId);
        post.setIsTop(isTop ? 1 : 0);
        postMapper.updateById(post);

        // 清除缓存
        redisUtil.delete(POST_CACHE_PREFIX + postId);

        log.info("帖子置顶状态更新: postId={}, isTop={}", postId, isTop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void essencePost(Long postId, boolean isEssence) {
        Post post = getPostById(postId);
        post.setIsEssence(isEssence ? 1 : 0);
        postMapper.updateById(post);

        // 清除缓存
        redisUtil.delete(POST_CACHE_PREFIX + postId);

        log.info("帖子精华状态更新: postId={}, isEssence={}", postId, isEssence);
    }

    @Override
    public Post getPostById(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException("帖子不存在或已被删除");
        }
        return post;
    }

    /**
     * 转换为帖子列表响应
     */
    private PostResponse convertToResponse(Post post) {
        User author = userMapper.selectById(post.getAuthorId());
        Category category = categoryMapper.selectById(post.getCategoryId());

        // 生成内容摘要（去除HTML标签，截取前200字符）
        String summary = post.getContent();
        if (summary != null) {
            summary = summary.replaceAll("<[^>]+>", "");
            if (summary.length() > 200) {
                summary = summary.substring(0, 200) + "...";
            }
        }

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .summary(summary)
                .authorName(author != null ? author.getUsername() : "未知用户")
                .authorId(post.getAuthorId())
                .categoryName(category != null ? category.getName() : "未分类")
                .categoryId(post.getCategoryId())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isTop(post.getIsTop())
                .isEssence(post.getIsEssence())
                .createTime(post.getCreateTime())
                .lastCommentTime(post.getLastCommentTime())
                .build();
    }

    /**
     * 转换为分类响应
     */
    private CategoryResponse convertToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .sortOrder(category.getSortOrder())
                .build();
    }
}