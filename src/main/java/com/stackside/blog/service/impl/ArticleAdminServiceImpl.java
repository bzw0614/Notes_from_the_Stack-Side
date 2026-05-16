package com.stackside.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stackside.blog.common.constant.RedisKeyConstants;
import com.stackside.blog.common.exception.BusinessException;
import com.stackside.blog.mapper.BlogArticleMapper;
import com.stackside.blog.mapper.BlogArticleTagMapper;
import com.stackside.blog.model.dto.ArticleSaveDTO;
import com.stackside.blog.model.entity.BlogArticle;
import com.stackside.blog.model.entity.BlogArticleTag;
import com.stackside.blog.service.ArticleAdminService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class ArticleAdminServiceImpl extends ServiceImpl<BlogArticleMapper, BlogArticle> implements ArticleAdminService {

    private final BlogArticleTagMapper articleTagMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public ArticleAdminServiceImpl(BlogArticleTagMapper articleTagMapper,
                                   @Qualifier("blogStringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.articleTagMapper = articleTagMapper;
        this.redisTemplate = redisTemplate;
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
        article.setIsTop(dto.getIsTop());
        article.setStatus(dto.getStatus());
        return article;
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
