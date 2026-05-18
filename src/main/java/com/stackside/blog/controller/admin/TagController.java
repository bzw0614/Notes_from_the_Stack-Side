package com.stackside.blog.controller.admin;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.stackside.blog.common.result.Result;
import com.stackside.blog.mapper.BlogArticleTagMapper;
import com.stackside.blog.mapper.BlogTagMapper;
import com.stackside.blog.model.dto.TagSaveDTO;
import com.stackside.blog.model.entity.BlogArticleTag;
import com.stackside.blog.model.entity.BlogTag;
import com.stackside.blog.model.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Tag APIs")
@Validated
@RestController
@RequiredArgsConstructor
public class TagController {

    private final BlogTagMapper tagMapper;
    private final BlogArticleTagMapper articleTagMapper;

    @Operation(summary = "List front tags")
    @GetMapping("/api/front/tags")
    public Result<List<TagVO>> frontList() {
        return Result.success(listTags());
    }

    @Operation(summary = "List admin tags")
    @GetMapping("/admin/tags")
    public Result<List<TagVO>> adminList() {
        return Result.success(listTags());
    }

    @Operation(summary = "Create tag")
    @PostMapping("/admin/tags")
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> create(@Valid @RequestBody TagSaveDTO dto) {
        BlogTag tag = new BlogTag();
        tag.setName(dto.getName());
        tagMapper.insert(tag);
        return Result.success(tag.getId());
    }

    @Operation(summary = "Update tag")
    @PutMapping("/admin/tags/{tagId}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> update(@PathVariable @Min(1) Long tagId,
                               @Valid @RequestBody TagSaveDTO dto) {
        BlogTag tag = new BlogTag();
        tag.setId(tagId);
        tag.setName(dto.getName());
        tagMapper.updateById(tag);
        return Result.success();
    }

    @Operation(summary = "Delete tag")
    @DeleteMapping("/admin/tags/{tagId}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(@PathVariable @Min(1) Long tagId) {
        tagMapper.deleteById(tagId);
        articleTagMapper.delete(
                Wrappers.lambdaQuery(BlogArticleTag.class)
                        .eq(BlogArticleTag::getTagId, tagId)
        );
        return Result.success();
    }

    private List<TagVO> listTags() {
        return tagMapper.selectList(Wrappers.lambdaQuery(BlogTag.class).orderByAsc(BlogTag::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    private TagVO toVO(BlogTag tag) {
        Long articleCount = articleTagMapper.selectCount(
                Wrappers.lambdaQuery(BlogArticleTag.class)
                        .eq(BlogArticleTag::getTagId, tag.getId())
        );
        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .articleCount(articleCount)
                .createTime(tag.getCreateTime())
                .updateTime(tag.getUpdateTime())
                .build();
    }
}
