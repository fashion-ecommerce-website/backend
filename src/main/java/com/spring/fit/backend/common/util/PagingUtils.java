package com.spring.fit.backend.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class PagingUtils {

    // Allowed sort fields for security
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "createdAt", "updatedAt", "subtotalAmount", "totalAmount", 
        "discountAmount", "shippingFee", "status", "paymentStatus"
    );

    /**
     * Build Pageable with sorting validation and direction parsing
     * @param pageable Original pageable
     * @param sortBy Sort field name
     * @param direction Sort direction (asc/desc)
     * @return Pageable with applied sorting
     */
    public static Pageable buildPageableWithSorting(Pageable pageable, String sortBy, String direction) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return pageable;
        }

        // Validate field names to prevent SQL injection
        if (!isValidSortField(sortBy)) {
            log.warn("Invalid sort field: {}, using default sorting", sortBy);
            return pageable;
        }

        // Parse direction and create sort
        Sort.Direction sortDirection = parseSortDirection(direction);
        Sort sort = Sort.by(sortDirection, sortBy);

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    /**
     * Parse sort direction string to Sort.Direction enum
     * @param direction Direction string (asc/desc)
     * @return Sort.Direction enum
     */
    public static Sort.Direction parseSortDirection(String direction) {
        if (direction == null || direction.trim().isEmpty()) {
            return Sort.Direction.ASC;
        }
        
        String normalizedDirection = direction.trim().toUpperCase();
        return "DESC".equals(normalizedDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    /**
     * Validate if sort field is allowed (security check)
     * @param field Field name to validate
     * @return true if field is allowed
     */
    public static boolean isValidSortField(String field) {
        return ALLOWED_SORT_FIELDS.contains(field);
    }

    /**
     * Get all allowed sort fields
     * @return Set of allowed sort field names
     */
    public static Set<String> getAllowedSortFields() {
        return Set.copyOf(ALLOWED_SORT_FIELDS);
    }
}
