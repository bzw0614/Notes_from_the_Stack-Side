package com.stackside.blog.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("blog_article")
@Schema(description = "文章信息实体")
public class BlogArticle implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "文章ID")
    private Long id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章摘要")
    private String summary;

    @Schema(description = "Markdown 正文内容")
    private String content;

    @TableField("cover_img")
    @Schema(description = "封面图URL")
    private String coverImg;

    @TableField("category_id")
    @Schema(description = "所属分类ID")
    private Long categoryId;

    @TableField("view_count")
    @Schema(description = "阅读量")
    private Integer viewCount;

    @TableField("is_top")
    @Schema(description = "是否置顶，0 否 1 是")
    private Integer isTop;

    @Schema(description = "状态，0 草稿 1 已发布")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    @Schema(description = "逻辑删除，0 未删 1 已删")
    private Integer isDeleted;
}
