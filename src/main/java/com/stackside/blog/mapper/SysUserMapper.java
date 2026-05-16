package com.stackside.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stackside.blog.model.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
