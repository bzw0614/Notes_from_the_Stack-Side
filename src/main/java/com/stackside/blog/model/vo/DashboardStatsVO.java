package com.stackside.blog.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin dashboard stats")
public class DashboardStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long articleCount;

    private Long publishedCount;

    private Long draftCount;

    private Long categoryCount;

    private Long tagCount;

    private Long viewCount;

    private List<ArticleListItemVO> latestArticles;
}
