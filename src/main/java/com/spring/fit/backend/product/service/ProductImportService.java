package com.spring.fit.backend.product.service;

import com.spring.fit.backend.product.domain.dto.response.ProductDetailPreviewResponse;
import com.spring.fit.backend.product.domain.dto.response.ProductGroupResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface  ProductImportService {

    List<ProductGroupResponse> parseCsv(MultipartFile file);
    void saveAllImportedProducts(List<ProductGroupResponse> groups);
}
