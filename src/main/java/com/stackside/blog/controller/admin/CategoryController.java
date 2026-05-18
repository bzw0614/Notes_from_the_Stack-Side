package com.stackside.blog.controller.admin;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.stackside.blog.common.result.Result;
import com.stackside.blog.mapper.BlogArticleMapper;
import com.stackside.blog.mapper.BlogCategoryMapper;
import com.stackside.blog.model.dto.CategorySaveDTO;
import com.stackside.blog.model.entity.BlogArticle;
import com.stackside.blog.model.entity.BlogCategory;
import com.stackside.blog.model.vo.CategoryVO;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Tag(name = "Category APIs")
@Validated
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final BlogCategoryMapper categoryMapper;
    private final BlogArticleMapper articleMapper;

    @Operation(summary = "List front categories")
    @GetMapping("/api/front/categories")
    public Result<List<CategoryVO>> frontList() {
        return Result.success(listCategories());
    }

    @Operation(summary = "List admin categories")
    @GetMapping("/admin/categories")
    public Result<List<CategoryVO>> adminList() {
        return Result.success(listCategories());
    }

    @Operation(summary = "Create category")
    @PostMapping("/admin/categories")
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> create(@Valid @RequestBody CategorySaveDTO dto) {
        BlogCategory category = new BlogCategory();
        category.setName(dto.getName());
        category.setSort(Optional.ofNullable(dto.getSort()).orElse(0));
        categoryMapper.insert(category);
        return Result.success(category.getId());
    }

    @Operation(summary = "Update category")
    @PutMapping("/admin/categories/{categoryId}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> update(@PathVariable @Min(1) Long categoryId,
                               @Valid @RequestBody CategorySaveDTO dto) {
        BlogCategory category = new BlogCategory();
        category.setId(categoryId);
        category.setName(dto.getName());
        category.setSort(Optional.ofNullable(dto.getSort()).orElse(0));
        categoryMapper.updateById(category);
        return Result.success();
    }

    @Operation(summary = "Delete category")
    @DeleteMapping("/admin/categories/{categoryId}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(@PathVariable @Min(1) Long categoryId) {
        categoryMapper.deleteById(categoryId);
        return Result.success();
    }

    private List<CategoryVO> listCategories() {
        return categoryMapper.selectList(
                        Wrappers.lambdaQuery(BlogCategory.class)
                                .orderByDesc(BlogCategory::getSort)
                                .orderByAsc(BlogCategory::getId)
                )
                .stream()
                .map(this::toVO)
                .toList();
    }

    private CategoryVO toVO(BlogCategory category) {
        Long articleCount = articleMapper.selectCount(
                Wrappers.lambdaQuery(BlogArticle.class)
                        .eq(BlogArticle::getCategoryId, category.getId())
        );
        return CategoryVO.builder()
                .id(category.getId())
                .name(category.getName())
                .sort(category.getSort())
                .articleCount(articleCount)
                .createTime(category.getCreateTime())
                .updateTime(category.getUpdateTime())
                .build();
    }
}
