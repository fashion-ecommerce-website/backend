package com.spring.fit.backend.category.service;

import com.spring.fit.backend.category.domain.dto.CategoryRequest;
import com.spring.fit.backend.category.domain.dto.CategoryResponse;
import com.spring.fit.backend.common.model.response.PageResult;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getActiveCategoryTree();

    List<CategoryResponse> getCategoryTree();


    PageResult<CategoryResponse> getAllCategories(int page, int pageSize);

    CategoryResponse getCategoryTreeByParent(Long parentId);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void toggleCategoryStatus(Long id);

    PageResult<CategoryResponse> searchCategories(String name, Boolean isActive, int page, int pageSize);

}
