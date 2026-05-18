package com.stackside.blog.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Article list item")
public class ArticleListItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Article id")
    private Long id;

    @Schema(description = "Article title")
    private String title;

    @Schema(description = "Article summary")
    private String summary;

    @Schema(description = "Cover image URL")
    private String coverImg;

    @Schema(description = "Category id")
    private Long categoryId;

    @Schema(description = "Category name")
    private String categoryName;

    @Schema(description = "Tags")
    private List<TagVO> tags;

    @Schema(description = "View count")
    private Integer viewCount;

    @Schema(description = "Pinned flag")
    private Integer isTop;

    @Schema(description = "Article status, 0 draft, 1 published")
    private Integer status;

    @Schema(description = "Create time")
    private LocalDateTime createTime;

    @Schema(description = "Update time")
    private LocalDateTime updateTime;
}
