package com.task.library_managment_system.dto.category;

import com.task.library_managment_system.models.Category;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryRequestBody {
    @NotNull
    private RequestCategory category;
    @NotNull
    private Long parentId;
}
