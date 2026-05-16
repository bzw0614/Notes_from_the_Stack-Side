package com.stackside.blog.controller.front;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.stackside.blog.common.result.Result;
import com.stackside.blog.model.vo.ArticleDetailVO;
import com.stackside.blog.model.vo.ArticleListItemVO;
import com.stackside.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "前台文章展示接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/articles")
public class ArticleFrontController {

    private final ArticleService articleService;

    @Operation(summary = "分页获取首页文章列表")
    @GetMapping
    public Result<IPage<ArticleListItemVO>> pageHomeArticles(
            @RequestParam(defaultValue = "1") @Min(1) long pageNo,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) long pageSize) {
        return Result.success(articleService.getHomeArticlePage(pageNo, pageSize));
    }

    @Operation(summary = "获取文章详情")
    @GetMapping("/{articleId}")
    public Result<ArticleDetailVO> getArticleDetail(@PathVariable @Min(1) Long articleId) {
        return Result.success(articleService.getArticleDetail(articleId));
    }
}
