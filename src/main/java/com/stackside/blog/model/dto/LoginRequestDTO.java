package com.stackside.blog.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "后台登录请求")
public class LoginRequestDTO {

    @NotBlank(message = "username is required")
    @Schema(description = "登录账号")
    private String username;

    @NotBlank(message = "password is required")
    @Schema(description = "登录密码")
    private String password;
}
