package com.stackside.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stackside.blog.common.constant.RedisKeyConstants;
import com.stackside.blog.common.exception.BusinessException;
import com.stackside.blog.mapper.BlogArticleMapper;
import com.stackside.blog.mapper.BlogArticleTagMapper;
import com.stackside.blog.mapper.BlogCategoryMapper;
import com.stackside.blog.mapper.BlogTagMapper;
import com.stackside.blog.model.dto.ArticleSaveDTO;
import com.stackside.blog.model.entity.BlogArticle;
import com.stackside.blog.model.entity.BlogArticleTag;
import com.stackside.blog.model.entity.BlogCategory;
import com.stackside.blog.model.entity.BlogTag;
import com.stackside.blog.model.vo.ArticleDetailVO;
import com.stackside.blog.model.vo.ArticleListItemVO;
import com.stackside.blog.model.vo.DashboardStatsVO;
import com.stackside.blog.model.vo.TagVO;
import com.stackside.blog.service.ArticleAdminService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ArticleAdminServiceImpl extends ServiceImpl<BlogArticleMapper, BlogArticle> implements ArticleAdminService {

    private final BlogArticleTagMapper articleTagMapper;
    private final BlogCategoryMapper categoryMapper;
    private final BlogTagMapper tagMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public ArticleAdminServiceImpl(BlogArticleTagMapper articleTagMapper,
                                   BlogCategoryMapper categoryMapper,
                                   BlogTagMapper tagMapper,
                                   @Qualifier("blogStringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.articleTagMapper = articleTagMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<ArticleListItemVO> pageAdminArticles(long pageNo, long pageSize, String keyword, Integer status) {
        final String finalKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        Page<BlogArticle> articlePage = baseMapper.selectPage(
                new Page<>(pageNo, pageSize),
                Wrappers.lambdaQuery(BlogArticle.class)
                        .eq(status != null, BlogArticle::getStatus, status)
                        .and(finalKeyword != null, wrapper -> wrapper
                                .like(BlogArticle::getTitle, finalKeyword)
                                .or()
                                .like(BlogArticle::getSummary, finalKeyword))
                        .orderByDesc(BlogArticle::getUpdateTime)
                        .orderByDesc(BlogArticle::getCreateTime)
        );

        List<BlogArticle> articles = articlePage.getRecords();
        Page<ArticleListItemVO> resultPage = new Page<>(articlePage.getCurrent(), articlePage.getSize());
        resultPage.setTotal(articlePage.getTotal());
        if (CollectionUtils.isEmpty(articles)) {
            resultPage.setRecords(Collections.emptyList());
            return resultPage;
        }

        Map<Long, String> categoryNameMap = queryCategoryNameMap(articles);
        Map<Long, List<TagVO>> articleTagMap = queryArticleTagMap(articles.stream().map(BlogArticle::getId).toList());
        List<ArticleListItemVO> records = articles.stream()
                .map(article -> buildListItem(article, categoryNameMap, articleTagMap))
                .toList();
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDetailVO getAdminArticleDetail(Long articleId) {
        BlogArticle article = getById(articleId);
        if (article == null) {
            throw new BusinessException(404, "Article not found");
        }
        Map<Long, String> categoryNameMap = queryCategoryNameMap(List.of(article));
        Map<Long, List<TagVO>> articleTagMap = queryArticleTagMap(List.of(articleId));
        return ArticleDetailVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(article.getContent())
                .coverImg(article.getCoverImg())
                .categoryId(article.getCategoryId())
                .categoryName(categoryNameMap.get(article.getCategoryId()))
                .tags(articleTagMap.getOrDefault(article.getId(), Collections.emptyList()))
                .viewCount(Optional.ofNullable(article.getViewCount()).orElse(0))
                .isTop(article.getIsTop())
                .status(article.getStatus())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsVO getDashboardStats() {
        Long articleCount = count();
        Long publishedCount = lambdaQuery().eq(BlogArticle::getStatus, 1).count();
        Long draftCount = lambdaQuery().eq(BlogArticle::getStatus, 0).count();
        Long categoryCount = categoryMapper.selectCount(Wrappers.lambdaQuery(BlogCategory.class));
        Long tagCount = tagMapper.selectCount(Wrappers.lambdaQuery(BlogTag.class));
        Long viewCount = Optional.ofNullable(baseMapper.selectList(Wrappers.lambdaQuery(BlogArticle.class))
                        .stream()
                        .map(BlogArticle::getViewCount)
                        .filter(Objects::nonNull)
                        .mapToLong(Integer::longValue)
                        .sum())
                .orElse(0L);
        List<ArticleListItemVO> latestArticles = pageAdminArticles(1, 5, null, null).getRecords();
        return DashboardStatsVO.builder()
                .articleCount(articleCount)
                .publishedCount(publishedCount)
                .draftCount(draftCount)
                .categoryCount(categoryCount)
                .tagCount(tagCount)
                .viewCount(viewCount)
                .latestArticles(latestArticles)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishArticle(ArticleSaveDTO dto) {
        BlogArticle article = toEntity(dto);
        article.setId(null);
        article.setViewCount(0);
        save(article);
        replaceArticleTags(article.getId(), dto.getTagIds());
        clearArticleDetailCache(article.getId());
        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long articleId, ArticleSaveDTO dto) {
        BlogArticle article = getById(articleId);
        if (article == null) {
            throw new BusinessException(404, "Article not found");
        }
        BlogArticle update = toEntity(dto);
        update.setId(articleId);
        update.setViewCount(null);
        updateById(update);
        replaceArticleTags(articleId, dto.getTagIds());
        clearArticleDetailCache(articleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineArticle(Long articleId) {
        BlogArticle article = getById(articleId);
        if (article == null) {
            throw new BusinessException(404, "Article not found");
        }
        BlogArticle update = new BlogArticle();
        update.setId(articleId);
        update.setStatus(0);
        updateById(update);
        clearArticleDetailCache(articleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long articleId) {
        BlogArticle article = getById(articleId);
        if (article == null) {
            throw new BusinessException(404, "Article not found");
        }
        removeById(articleId);
        articleTagMapper.delete(
                Wrappers.<BlogArticleTag>lambdaQuery()
                        .eq(BlogArticleTag::getArticleId, articleId)
        );
        clearArticleAllCache(articleId);
    }

    private BlogArticle toEntity(ArticleSaveDTO dto) {
        BlogArticle article = new BlogArticle();
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCoverImg(dto.getCoverImg());
        article.setCategoryId(dto.getCategoryId());
        article.setIsTop(Optional.ofNullable(dto.getIsTop()).orElse(0));
        article.setStatus(Optional.ofNullable(dto.getStatus()).orElse(1));
        return article;
    }

    private ArticleListItemVO buildListItem(BlogArticle article,
                                            Map<Long, String> categoryNameMap,
                                            Map<Long, List<TagVO>> articleTagMap) {
        return ArticleListItemVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .coverImg(article.getCoverImg())
                .categoryId(article.getCategoryId())
                .categoryName(categoryNameMap.get(article.getCategoryId()))
                .tags(articleTagMap.getOrDefault(article.getId(), Collections.emptyList()))
                .viewCount(Optional.ofNullable(article.getViewCount()).orElse(0))
                .isTop(article.getIsTop())
                .status(article.getStatus())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build();
    }

    private Map<Long, String> queryCategoryNameMap(List<BlogArticle> articles) {
        List<Long> categoryIds = articles.stream()
                .map(BlogArticle::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(categoryIds)) {
            return Collections.emptyMap();
        }
        return categoryMapper.selectBatchIds(categoryIds)
                .stream()
                .collect(Collectors.toMap(BlogCategory::getId, BlogCategory::getName, (left, right) -> left));
    }

    private Map<Long, List<TagVO>> queryArticleTagMap(List<Long> articleIds) {
        if (CollectionUtils.isEmpty(articleIds)) {
            return Collections.emptyMap();
        }
        List<BlogArticleTag> relations = articleTagMapper.selectList(
                Wrappers.lambdaQuery(BlogArticleTag.class)
                        .in(BlogArticleTag::getArticleId, articleIds)
        );
        if (CollectionUtils.isEmpty(relations)) {
            return Collections.emptyMap();
        }
        List<Long> tagIds = relations.stream()
                .map(BlogArticleTag::getTagId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(tagIds)) {
            return Collections.emptyMap();
        }
        Map<Long, BlogTag> tagMap = tagMapper.selectBatchIds(tagIds)
                .stream()
                .collect(Collectors.toMap(BlogTag::getId, Function.identity(), (left, right) -> left));
        return relations.stream()
                .map(relation -> new AbstractMap.SimpleImmutableEntry<>(relation.getArticleId(), tagMap.get(relation.getTagId())))
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(entry -> toTagVO(entry.getValue()), Collectors.toList())
                ));
    }

    private TagVO toTagVO(BlogTag tag) {
        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .createTime(tag.getCreateTime())
                .updateTime(tag.getUpdateTime())
                .build();
    }

    private void replaceArticleTags(Long articleId, List<Long> tagIds) {
        articleTagMapper.delete(
                Wrappers.<BlogArticleTag>lambdaQuery()
                        .eq(BlogArticleTag::getArticleId, articleId)
        );
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        List<BlogArticleTag> relations = tagIds.stream()
                .distinct()
                .map(tagId -> {
                    BlogArticleTag relation = new BlogArticleTag();
                    relation.setArticleId(articleId);
                    relation.setTagId(tagId);
                    return relation;
                })
                .toList();
        relations.forEach(articleTagMapper::insert);
    }

    private void clearArticleDetailCache(Long articleId) {
        redisTemplate.opsForHash().delete(RedisKeyConstants.ARTICLE_DETAIL_HASH, articleId.toString());
    }

    private void clearArticleAllCache(Long articleId) {
        clearArticleDetailCache(articleId);
        redisTemplate.opsForHash().delete(RedisKeyConstants.ARTICLE_VIEW_COUNT_HASH, articleId.toString());
    }
}
