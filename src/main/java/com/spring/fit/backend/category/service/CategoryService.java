package com.spring.fit.backend.category.service;

import com.spring.fit.backend.category.domain.dto.CategoryRequest;
import com.spring.fit.backend.category.domain.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getCategoryTree();

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryTreeByParent(Long parentId);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

}
