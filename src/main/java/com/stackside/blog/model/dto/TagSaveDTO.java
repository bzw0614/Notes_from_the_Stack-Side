package com.stackside.blog.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Tag save request")
public class TagSaveDTO {

    @NotBlank(message = "name is required")
    @Schema(description = "Tag name")
    private String name;
}
