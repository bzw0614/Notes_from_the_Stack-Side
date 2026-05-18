package com.stackside.blog.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Category save request")
public class CategorySaveDTO {

    @NotBlank(message = "name is required")
    @Schema(description = "Category name")
    private String name;

    @Schema(description = "Sort weight")
    private Integer sort;
}
