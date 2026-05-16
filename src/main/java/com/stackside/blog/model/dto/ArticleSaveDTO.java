package com.stackside.blog.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "文章保存请求")
public class ArticleSaveDTO {

    @Schema(description = "文章ID，更新时传入")
    private Long id;

    @NotBlank(message = "title is required")
    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章摘要")
    private String summary;

    @NotBlank(message = "content is required")
    @Schema(description = "Markdown 正文")
    private String content;

    @Schema(description = "封面图URL")
    private String coverImg;

    @NotNull(message = "categoryId is required")
    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "状态，0 草稿 1 已发布")
    private Integer status;
}
