package com.stackside.blog.common.exception;

import com.stackside.blog.common.result.Result;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        this(Result.FAIL_CODE, message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
