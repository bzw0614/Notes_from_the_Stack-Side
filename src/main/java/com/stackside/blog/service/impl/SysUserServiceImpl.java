package com.stackside.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stackside.blog.common.exception.BusinessException;
import com.stackside.blog.mapper.SysUserMapper;
import com.stackside.blog.model.entity.SysUser;
import com.stackside.blog.service.SysUserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public SysUser verifyLogin(String username, String password) {
        SysUser user = lambdaQuery()
                .eq(SysUser::getUsername, username)
                .oneOpt()
                .orElseThrow(() -> new BusinessException(401, "Invalid username or password"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "Invalid username or password");
        }
        return user;
    }

}
