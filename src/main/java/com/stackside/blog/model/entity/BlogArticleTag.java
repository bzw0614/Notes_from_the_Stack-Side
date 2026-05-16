package com.stackside.blog.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("blog_article_tag")
@Schema(description = "文章标签关联实体")
public class BlogArticleTag implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField("article_id")
    @Schema(description = "文章ID")
    private Long articleId;

    @TableField("tag_id")
    @Schema(description = "标签ID")
    private Long tagId;
}
