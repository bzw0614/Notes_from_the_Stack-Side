package com.stackside.blog.controller;

import com.stackside.blog.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

    @GetMapping({"/", "/."})
    public Result<String> health() {
        return Result.success("stackside-backend is running");
    }
}
