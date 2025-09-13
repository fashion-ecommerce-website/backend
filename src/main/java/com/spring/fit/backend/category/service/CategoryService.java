package com.spring.fit.backend.category.service;

import com.spring.fit.backend.category.domain.dto.CategoryRequest;
import com.spring.fit.backend.category.domain.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getCategoryTree();

    CategoryResponse getCategoryTreeByParent(Long parentId);

    CategoryResponse getCategoryBySlug(String slug);

    CategoryResponse getCategoryByName(String name);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

}
