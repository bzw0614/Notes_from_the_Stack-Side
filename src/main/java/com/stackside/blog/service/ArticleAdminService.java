package com.stackside.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stackside.blog.model.entity.BlogArticle;
import com.stackside.blog.model.dto.ArticleSaveDTO;
import com.stackside.blog.model.vo.ArticleDetailVO;
import com.stackside.blog.model.vo.ArticleListItemVO;
import com.stackside.blog.model.vo.DashboardStatsVO;

public interface ArticleAdminService extends IService<BlogArticle> {

    com.baomidou.mybatisplus.core.metadata.IPage<ArticleListItemVO> pageAdminArticles(
            long pageNo,
            long pageSize,
            String keyword,
            Integer status
    );

    ArticleDetailVO getAdminArticleDetail(Long articleId);

    DashboardStatsVO getDashboardStats();

    Long publishArticle(ArticleSaveDTO dto);

    void updateArticle(Long articleId, ArticleSaveDTO dto);

    void offlineArticle(Long articleId);

    void deleteArticle(Long articleId);
}
