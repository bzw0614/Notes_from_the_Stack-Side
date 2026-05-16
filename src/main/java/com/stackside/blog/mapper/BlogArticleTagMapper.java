package com.stackside.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stackside.blog.model.entity.BlogArticleTag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogArticleTagMapper extends BaseMapper<BlogArticleTag> {
}
