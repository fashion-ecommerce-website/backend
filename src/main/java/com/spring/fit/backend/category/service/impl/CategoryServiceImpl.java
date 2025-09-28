package com.spring.fit.backend.category.service.impl;

import com.spring.fit.backend.category.domain.dto.CategoryRequest;
import com.spring.fit.backend.category.domain.dto.CategoryResponse;
import com.spring.fit.backend.category.domain.entity.Category;
import com.spring.fit.backend.category.repository.CategoryRepository;
import com.spring.fit.backend.category.service.CategoryService;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.common.model.response.PageResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getCategoryTree() {
        List<Category> all = categoryRepository.findAll();
        return CategoryResponse.buildTree(all);
    }

    @Override
    public CategoryResponse getCategoryTreeByParent(Long parentId) {
        List<Category> all = categoryRepository.findAll();
        CategoryResponse root = CategoryResponse.buildTree(parentId, all);
        if (root == null) {
            throw new ErrorException(HttpStatus.NOT_FOUND, "Parent category not found");
        }
        return root;
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()
                || request.getSlug() == null || request.getSlug().isBlank()) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Name and slug are required");
        }

        if (categoryRepository.existsByName(request.getName().trim())) {
            throw new ErrorException(HttpStatus.CONFLICT, "Category name already exists");
        }

        Category category = new Category();
        category.setName(request.getName().trim());
        category.setSlug(request.getSlug().trim());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Parent category not found"));
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved.getId(), saved.getName(), saved.getSlug(),saved.getIsActive(), null);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Category not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            category.setName(request.getName().trim());
        }
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            category.setSlug(request.getSlug().trim());
        }

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Parent category not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        Category updated = categoryRepository.save(category);
        return new CategoryResponse(updated.getId(), updated.getName(), updated.getSlug(), updated.getIsActive(), null);
    }


    @Override
    public PageResult<CategoryResponse> getAllCategories(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        return PageResult.from(categoryPage.map(c ->
                new CategoryResponse(
                        c.getId(),
                        c.getName(),
                        c.getSlug(),
                        c.getIsActive(),
                        null
                )
        ));
    }



    @Override
    public void toggleCategoryStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Category not found"));
        category.setIsActive(!category.getIsActive());
        Category updated = categoryRepository.save(category);
    }
    @Override
    public PageResult<CategoryResponse> searchCategories(String name, Boolean isActive, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Category> categoryPage;

        if (name != null && !name.isBlank() && isActive != null) {
            categoryPage = categoryRepository.findAllByNameContainingIgnoreCaseAndIsActive(name.trim(), isActive, pageable);
        } else if (name != null && !name.isBlank()) {
            categoryPage = categoryRepository.findAllByNameContainingIgnoreCase(name.trim(), pageable);
        } else if (isActive != null) {
            categoryPage = categoryRepository.findAllByIsActive(isActive, pageable);
        } else {
            categoryPage = categoryRepository.findAll(pageable);
        }

        return PageResult.from(categoryPage.map(c ->
                new CategoryResponse(
                        c.getId(),
                        c.getName(),
                        c.getSlug(),
                        c.getIsActive(),
                        null
                )
        ));
    }

}
