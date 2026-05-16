package com.stackside.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stackside.blog.model.entity.BlogArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BlogArticleMapper extends BaseMapper<BlogArticle> {

    int incrementViewCount(@Param("articleId") Long articleId, @Param("delta") Long delta);
}
