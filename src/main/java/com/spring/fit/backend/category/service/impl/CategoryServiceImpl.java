package com.spring.fit.backend.category.service.impl;

import com.spring.fit.backend.category.domain.dto.CategoryRequest;
import com.spring.fit.backend.category.domain.dto.CategoryResponse;
import com.spring.fit.backend.category.domain.entity.Category;
import com.spring.fit.backend.category.repository.CategoryRepository;
import com.spring.fit.backend.category.service.CategoryService;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;


    public List<CategoryResponse> getCategoryTree() {
        List<Category> roots = categoryRepository.findByParentIsNull();
        return roots.stream()
                .map(CategoryResponse::mapToResponseRecursively)
                .toList();
    }

    @Override
    public CategoryResponse getCategoryTreeByParent(Long parentId) {
        log.info("Fetching category tree with parentId: {}", parentId);
        if (parentId == null) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Parent ID is required");
        }

        Category parentCategory = categoryRepository.findById(parentId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Parent category not found"));

        return CategoryResponse.mapToResponseRecursively(parentCategory);
    }

    @Override
    public CategoryResponse getCategoryBySlug(String slug) {
        log.info("Fetching category with slug: {}", slug);
        if (isEmpty(slug)) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Slug is required");
        }
        Category category = categoryRepository.findBySlug(slug.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Category not found"));
        return CategoryResponse.mapToResponse(category);
    }

    @Override
    public CategoryResponse getCategoryByName(String name) {
        log.info("Fetching category with name: {}", name);
        if (isEmpty(name)) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        Category category = categoryRepository.findByName(name.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Category not found"));
        return CategoryResponse.mapToResponse(category);
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating category: {}", request.getName());

        // Validate
        if (request == null || isEmpty(request.getName()) || isEmpty(request.getSlug())) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Name and slug are required");
        }

        // Check duplicate name
        if (categoryRepository.existsByName(request.getName().trim())) {
            throw new ErrorException(HttpStatus.CONFLICT, "Category name already exists");
        }

        // Build category
        Category category = new Category();
        category.setName(request.getName().trim());
        category.setSlug(request.getSlug().trim());

        // Set parent if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Parent category not found"));
            category.setParent(parent);
        }

        // Save
        Category saved = categoryRepository.save(category);
        log.info("Category created with id: {}", saved.getId());

        return CategoryResponse.mapToResponseRecursively(saved);
    }


    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        log.info("Updating category id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Category not found"));

        // Update name (có check duplicate)
        updateField(
                category,
                request.getName(),
                category.getName(),
                category::setName,
                newName -> !newName.equalsIgnoreCase(category.getName())
                        && categoryRepository.existsByName(newName)
        );

        // Update slug (không cần check trùng)
        updateField(
                category,
                request.getSlug(),
                category.getSlug(),
                category::setSlug,
                null
        );

        // Update parent
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Parent category not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category updated = categoryRepository.save(category);
        log.info("Category updated id: {}", updated.getId());

        return CategoryResponse.mapToResponseRecursively(updated);
    }

    private void updateField(Category category, String newValue, String currentValue,
                             Consumer<String> setter,
                             Function<String, Boolean> duplicateChecker) {
        if (!isEmpty(newValue) && !newValue.trim().equals(currentValue)) {
            String value = newValue.trim();

            // Check duplicate if needed
            if (duplicateChecker != null && duplicateChecker.apply(value)) {
                throw new ErrorException(HttpStatus.CONFLICT, "Value already exists: " + value);
            }

            setter.accept(value);
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

}

