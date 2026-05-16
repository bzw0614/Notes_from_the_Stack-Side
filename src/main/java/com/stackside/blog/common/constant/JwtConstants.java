package com.stackside.blog.common.constant;

public final class JwtConstants {

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_HEADER_COMPAT = "Token";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";

    private JwtConstants() {
    }
}
