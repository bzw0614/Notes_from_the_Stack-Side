package com.stackside.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stackside.blog.model.entity.BlogArticle;
import com.stackside.blog.model.dto.ArticleSaveDTO;

public interface ArticleAdminService extends IService<BlogArticle> {

    Long publishArticle(ArticleSaveDTO dto);

    void updateArticle(Long articleId, ArticleSaveDTO dto);

    void offlineArticle(Long articleId);

    void deleteArticle(Long articleId);
}
