package com.spring.fit.backend.category.repository;

import com.spring.fit.backend.category.domain.entity.Category;
import com.spring.fit.backend.common.model.response.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIsNull();

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentId(Long parentId);

    boolean existsByName(String name);

    Page<Category> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Category> findAllByIsActive(Boolean isActive, Pageable pageable);

    Page<Category> findAllByNameContainingIgnoreCaseAndIsActive(String name, Boolean isActive, Pageable pageable);

}
