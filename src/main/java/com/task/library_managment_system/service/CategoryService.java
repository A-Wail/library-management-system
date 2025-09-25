package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.category.CategoryRequestBody;
import com.task.library_managment_system.dto.category.CategoryResponse;
import com.task.library_managment_system.dto.category.RequestCategory;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(RequestCategory category, Long parentId);
    List<CategoryResponse> getAllCategories();
    CategoryResponse viewCategoryById(Long categoryId);
    CategoryResponse updateCategory(Long categoryId, RequestCategory updatedCategory, Long parentId);
    void deleteCategory(Long categoryId);
}
