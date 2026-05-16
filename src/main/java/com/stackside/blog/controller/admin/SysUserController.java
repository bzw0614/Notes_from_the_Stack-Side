package com.stackside.blog.controller.admin;

import com.stackside.blog.common.result.Result;
import com.stackside.blog.model.dto.LoginRequestDTO;
import com.stackside.blog.model.entity.SysUser;
import com.stackside.blog.model.vo.LoginVO;
import com.stackside.blog.security.jwt.JwtUtils;
import com.stackside.blog.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "后台登录接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/sys-user")
public class SysUserController {

    private final SysUserService sysUserService;
    private final JwtUtils jwtUtils;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequestDTO request) {
        SysUser user = sysUserService.verifyLogin(request.getUsername(), request.getPassword());
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        return Result.success(LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build());
    }
}
