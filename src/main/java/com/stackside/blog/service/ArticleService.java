package com.stackside.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stackside.blog.model.entity.BlogArticle;
import com.stackside.blog.model.vo.ArticleDetailVO;
import com.stackside.blog.model.vo.ArticleListItemVO;

public interface ArticleService extends IService<BlogArticle> {

    IPage<ArticleListItemVO> getHomeArticlePage(long pageNo, long pageSize, String keyword, Long categoryId);

    ArticleDetailVO getArticleDetail(Long articleId);
}
