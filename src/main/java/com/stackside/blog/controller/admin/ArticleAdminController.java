package com.stackside.blog.controller.admin;

import com.stackside.blog.common.result.Result;
import com.stackside.blog.model.dto.ArticleSaveDTO;
import com.stackside.blog.service.ArticleAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "后台文章管理接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/articles")
public class ArticleAdminController {

    private final ArticleAdminService articleAdminService;

    @Operation(summary = "发布文章")
    @PostMapping
    public Result<Long> publish(@Valid @RequestBody ArticleSaveDTO dto) {
        return Result.success(articleAdminService.publishArticle(dto));
    }

    @Operation(summary = "编辑文章")
    @PutMapping("/{articleId}")
    public Result<Void> update(@PathVariable @Min(1) Long articleId,
                               @Valid @RequestBody ArticleSaveDTO dto) {
        articleAdminService.updateArticle(articleId, dto);
        return Result.success();
    }

    @Operation(summary = "下线文章")
    @PutMapping("/{articleId}/offline")
    public Result<Void> offline(@PathVariable @Min(1) Long articleId) {
        articleAdminService.offlineArticle(articleId);
        return Result.success();
    }

    @Operation(summary = "逻辑删除文章")
    @DeleteMapping("/{articleId}")
    public Result<Void> delete(@PathVariable @Min(1) Long articleId) {
        articleAdminService.deleteArticle(articleId);
        return Result.success();
    }
}
