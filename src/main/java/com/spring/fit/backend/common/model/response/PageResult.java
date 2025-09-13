package com.spring.fit.backend.common.model.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        int page,
        int pageSize,
        long totalItems,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PageResult<T> from(Page<T> p) {
        return new PageResult<>(
                p.getContent(),
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.hasNext(),
                p.hasPrevious()
        );
    }
}

