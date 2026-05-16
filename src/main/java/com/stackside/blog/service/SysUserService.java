package com.stackside.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stackside.blog.model.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    SysUser verifyLogin(String username, String password);
}
