package com.stackside.blog.security.interceptor;

import com.stackside.blog.common.constant.JwtConstants;
import com.stackside.blog.common.result.Result;
import com.stackside.blog.security.jwt.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(JwtConstants.TOKEN_HEADER);
        if (token == null || token.isBlank()) {
            token = request.getHeader(JwtConstants.TOKEN_HEADER_COMPAT);
        }
        if (token == null || token.isBlank()) {
            writeUnauthorized(response, "Missing token");
            return false;
        }
        if (token.startsWith(JwtConstants.TOKEN_PREFIX)) {
            token = token.substring(JwtConstants.TOKEN_PREFIX.length());
        }
        try {
            jwtUtils.parseToken(token);
            return true;
        } catch (Exception ex) {
            writeUnauthorized(response, "Invalid or expired token");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(401, message)));
    }
}
