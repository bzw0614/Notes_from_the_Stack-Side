package com.stackside.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stackside.blog.common.constant.RedisKeyConstants;
import com.stackside.blog.common.exception.BusinessException;
import com.stackside.blog.mapper.BlogArticleMapper;
import com.stackside.blog.mapper.BlogArticleTagMapper;
import com.stackside.blog.mapper.BlogCategoryMapper;
import com.stackside.blog.mapper.BlogTagMapper;
import com.stackside.blog.model.entity.BlogArticle;
import com.stackside.blog.model.entity.BlogArticleTag;
import com.stackside.blog.model.entity.BlogCategory;
import com.stackside.blog.model.entity.BlogTag;
import com.stackside.blog.model.vo.ArticleDetailVO;
import com.stackside.blog.model.vo.ArticleListItemVO;
import com.stackside.blog.model.vo.TagVO;
import com.stackside.blog.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<BlogArticleMapper, BlogArticle> implements ArticleService {

    private static final int ARTICLE_STATUS_PUBLISHED = 1;
    private static final Duration ARTICLE_DETAIL_CACHE_TTL = Duration.ofHours(12);

    private final BlogCategoryMapper categoryMapper;
    private final BlogTagMapper tagMapper;
    private final BlogArticleTagMapper articleTagMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public ArticleServiceImpl(BlogCategoryMapper categoryMapper,
                              BlogTagMapper tagMapper,
                              BlogArticleTagMapper articleTagMapper,
                              @Qualifier("blogStringRedisTemplate") RedisTemplate<String, String> redisTemplate,
                              ObjectMapper objectMapper) {
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.articleTagMapper = articleTagMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<ArticleListItemVO> getHomeArticlePage(long pageNo, long pageSize) {
        Page<BlogArticle> articlePage = baseMapper.selectPage(
                new Page<>(pageNo, pageSize),
                Wrappers.lambdaQuery(BlogArticle.class)
                        .eq(BlogArticle::getStatus, ARTICLE_STATUS_PUBLISHED)
                        .orderByDesc(BlogArticle::getIsTop)
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
        Map<Long, List<TagVO>> articleTagMap = queryArticleTagMap(
                articles.stream().map(BlogArticle::getId).toList()
        );
        Map<Long, Long> viewDeltaMap = queryViewDeltaMap(
                articles.stream().map(BlogArticle::getId).toList()
        );

        List<ArticleListItemVO> records = articles.stream()
                .map(article -> buildListItem(article, categoryNameMap, articleTagMap, viewDeltaMap))
                .toList();
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public ArticleDetailVO getArticleDetail(Long articleId) {
        if (articleId == null || articleId <= 0) {
            throw new BusinessException(400, "Invalid article id");
        }

        ArticleDetailVO detail = getArticleDetailFromCache(articleId)
                .orElseGet(() -> queryArticleDetailFromDb(articleId));

        long latestDelta = increaseArticleViewCount(articleId);
        detail.setViewCount(Math.toIntExact(Optional.ofNullable(detail.getViewCount()).orElse(0) + latestDelta));
        return detail;
    }

    private ArticleListItemVO buildListItem(BlogArticle article,
                                            Map<Long, String> categoryNameMap,
                                            Map<Long, List<TagVO>> articleTagMap,
                                            Map<Long, Long> viewDeltaMap) {
        return ArticleListItemVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .coverImg(article.getCoverImg())
                .categoryId(article.getCategoryId())
                .categoryName(categoryNameMap.get(article.getCategoryId()))
                .tags(articleTagMap.getOrDefault(article.getId(), Collections.emptyList()))
                .viewCount(mergeViewCount(article.getViewCount(), viewDeltaMap.get(article.getId())))
                .isTop(article.getIsTop())
                .createTime(article.getCreateTime())
                .build();
    }

    private ArticleDetailVO buildDetail(BlogArticle article,
                                        Map<Long, String> categoryNameMap,
                                        Map<Long, List<TagVO>> articleTagMap,
                                        Map<Long, Long> viewDeltaMap) {
        return ArticleDetailVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(article.getContent())
                .coverImg(article.getCoverImg())
                .categoryId(article.getCategoryId())
                .categoryName(categoryNameMap.get(article.getCategoryId()))
                .tags(articleTagMap.getOrDefault(article.getId(), Collections.emptyList()))
                .viewCount(mergeViewCount(article.getViewCount(), viewDeltaMap.get(article.getId())))
                .isTop(article.getIsTop())
                .status(article.getStatus())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build();
    }

    private ArticleDetailVO queryArticleDetailFromDb(Long articleId) {
        BlogArticle article = baseMapper.selectOne(
                Wrappers.lambdaQuery(BlogArticle.class)
                        .eq(BlogArticle::getId, articleId)
                        .eq(BlogArticle::getStatus, ARTICLE_STATUS_PUBLISHED)
                        .last("LIMIT 1")
        );
        if (article == null) {
            throw new BusinessException(404, "Article not found or unpublished");
        }

        ArticleDetailVO detail = buildDetail(
                article,
                queryCategoryNameMap(List.of(article)),
                queryArticleTagMap(List.of(articleId)),
                Collections.emptyMap()
        );
        cacheArticleDetail(detail);
        return detail;
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

    private Optional<ArticleDetailVO> getArticleDetailFromCache(Long articleId) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String cacheValue = hashOps.get(RedisKeyConstants.ARTICLE_DETAIL_HASH, articleId.toString());
        if (cacheValue == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(cacheValue, ArticleDetailVO.class));
        } catch (JsonProcessingException ex) {
            log.warn("Failed to deserialize article detail cache, articleId={}", articleId, ex);
            hashOps.delete(RedisKeyConstants.ARTICLE_DETAIL_HASH, articleId.toString());
            return Optional.empty();
        }
    }

    private void cacheArticleDetail(ArticleDetailVO detail) {
        try {
            redisTemplate.opsForHash().put(
                    RedisKeyConstants.ARTICLE_DETAIL_HASH,
                    detail.getId().toString(),
                    objectMapper.writeValueAsString(detail)
            );
            redisTemplate.expire(RedisKeyConstants.ARTICLE_DETAIL_HASH, ARTICLE_DETAIL_CACHE_TTL);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize article detail cache, articleId={}", detail.getId(), ex);
        }
    }

    private long increaseArticleViewCount(Long articleId) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        Long latestDelta = hashOps.increment(RedisKeyConstants.ARTICLE_VIEW_COUNT_HASH, articleId.toString(), 1L);
        return latestDelta == null ? 1L : latestDelta;
    }

    private Map<Long, Long> queryViewDeltaMap(List<Long> articleIds) {
        if (CollectionUtils.isEmpty(articleIds)) {
            return Collections.emptyMap();
        }

        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        List<String> fields = articleIds.stream().map(String::valueOf).toList();
        List<String> cachedDeltas = hashOps.multiGet(RedisKeyConstants.ARTICLE_VIEW_COUNT_HASH, fields);
        final List<String> deltas = cachedDeltas == null
                ? Collections.nCopies(fields.size(), (String) null)
                : cachedDeltas;

        return IntStream.range(0, articleIds.size())
                .boxed()
                .collect(Collectors.toMap(
                        articleIds::get,
                        index -> parseLongOrZero(deltas.get(index)),
                        (left, right) -> left
                ));
    }

    private Integer mergeViewCount(Integer dbViewCount, Long redisDelta) {
        return Math.toIntExact(Optional.ofNullable(dbViewCount).orElse(0) + Optional.ofNullable(redisDelta).orElse(0L));
    }

    private Long parseLongOrZero(String value) {
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private TagVO toTagVO(BlogTag tag) {
        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
}
