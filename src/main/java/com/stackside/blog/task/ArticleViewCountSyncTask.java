package com.stackside.blog.task;

import com.stackside.blog.common.constant.RedisKeyConstants;
import com.stackside.blog.mapper.BlogArticleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class ArticleViewCountSyncTask {

    private static final DefaultRedisScript<Long> DELETE_ZERO_DELTA_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('HGET', KEYS[1], ARGV[1]) == '0' then return redis.call('HDEL', KEYS[1], ARGV[1]) else return 0 end",
            Long.class
    );

    private final BlogArticleMapper articleMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public ArticleViewCountSyncTask(BlogArticleMapper articleMapper,
                                    @Qualifier("blogStringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.articleMapper = articleMapper;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedDelayString = "${stackside.article.view-count-sync-delay:60000}")
    @Transactional(rollbackFor = Exception.class)
    public void syncViewCountToMysql() {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        Map<String, String> entries = hashOps.entries(RedisKeyConstants.ARTICLE_VIEW_COUNT_HASH);
        if (CollectionUtils.isEmpty(entries)) {
            return;
        }

        List<ViewCountDelta> deltas = entries.entrySet()
                .stream()
                .map(this::toViewCountDelta)
                .flatMap(Optional::stream)
                .filter(delta -> delta.delta() > 0)
                .toList();
        if (CollectionUtils.isEmpty(deltas)) {
            return;
        }

        deltas.forEach(delta -> articleMapper.incrementViewCount(delta.articleId(), delta.delta()));
        deltas.forEach(this::subtractSyncedDelta);
        hashOps.delete(
                RedisKeyConstants.ARTICLE_DETAIL_HASH,
                deltas.stream().map(delta -> delta.articleId().toString()).toArray()
        );
        log.info("Synced article view count to MySQL, articleCount={}", deltas.size());
    }

    private Optional<ViewCountDelta> toViewCountDelta(Map.Entry<String, String> entry) {
        try {
            return Optional.of(new ViewCountDelta(Long.parseLong(entry.getKey()), Long.parseLong(entry.getValue())));
        } catch (NumberFormatException ex) {
            log.warn("Ignored illegal article view count cache field, field={}, value={}", entry.getKey(), entry.getValue());
            return Optional.empty();
        }
    }

    private void subtractSyncedDelta(ViewCountDelta delta) {
        Long remaining = redisTemplate.opsForHash().increment(
                RedisKeyConstants.ARTICLE_VIEW_COUNT_HASH,
                delta.articleId().toString(),
                -delta.delta()
        );
        if (remaining != null && remaining == 0L) {
            redisTemplate.execute(
                    DELETE_ZERO_DELTA_SCRIPT,
                    Collections.singletonList(RedisKeyConstants.ARTICLE_VIEW_COUNT_HASH),
                    delta.articleId().toString()
            );
        }
    }

    private record ViewCountDelta(Long articleId, Long delta) {
    }
}
