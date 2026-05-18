package com.stackside.blog.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.stackside.blog.common.result.Result;
import com.stackside.blog.model.dto.ArticleSaveDTO;
import com.stackside.blog.model.vo.ArticleDetailVO;
import com.stackside.blog.model.vo.ArticleListItemVO;
import com.stackside.blog.service.ArticleAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin article APIs")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/articles")
public class ArticleAdminController {

    private final ArticleAdminService articleAdminService;

    @Operation(summary = "Page admin articles")
    @GetMapping
    public Result<IPage<ArticleListItemVO>> pageArticles(
            @RequestParam(defaultValue = "1") @Min(1) long pageNo,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(articleAdminService.pageAdminArticles(pageNo, pageSize, keyword, status));
    }

    @Operation(summary = "Get admin article detail")
    @GetMapping("/{articleId}")
    public Result<ArticleDetailVO> detail(@PathVariable @Min(1) Long articleId) {
        return Result.success(articleAdminService.getAdminArticleDetail(articleId));
    }

    @Operation(summary = "Publish article")
    @PostMapping
    public Result<Long> publish(@Valid @RequestBody ArticleSaveDTO dto) {
        return Result.success(articleAdminService.publishArticle(dto));
    }

    @Operation(summary = "Update article")
    @PutMapping("/{articleId}")
    public Result<Void> update(@PathVariable @Min(1) Long articleId,
                               @Valid @RequestBody ArticleSaveDTO dto) {
        articleAdminService.updateArticle(articleId, dto);
        return Result.success();
    }

    @Operation(summary = "Offline article")
    @PutMapping("/{articleId}/offline")
    public Result<Void> offline(@PathVariable @Min(1) Long articleId) {
        articleAdminService.offlineArticle(articleId);
        return Result.success();
    }

    @Operation(summary = "Delete article")
    @DeleteMapping("/{articleId}")
    public Result<Void> delete(@PathVariable @Min(1) Long articleId) {
        articleAdminService.deleteArticle(articleId);
        return Result.success();
    }
}
