package com.stackside.blog.common.constant;

public final class RedisKeyConstants {

    public static final String ARTICLE_DETAIL_HASH = "stackside:blog:article:detail";

    public static final String ARTICLE_VIEW_COUNT_HASH = "stackside:blog:article:view-count:delta";

    private RedisKeyConstants() {
    }
}
