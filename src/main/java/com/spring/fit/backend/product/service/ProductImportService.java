package com.spring.fit.backend.product.service;

import com.spring.fit.backend.product.domain.dto.request.ProductDetailCheckRequest;
import com.spring.fit.backend.product.domain.dto.response.ProductDetailCheckResponse;
import com.spring.fit.backend.product.domain.dto.response.ProductDetailPreviewResponse;
import com.spring.fit.backend.product.domain.dto.response.ProductGroupResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface  ProductImportService {

    ProductDetailCheckResponse checkProductDetail(ProductDetailCheckRequest request);

    List<ProductGroupResponse> parseCsvWithZips(MultipartFile csvFile, List<MultipartFile> zipFiles);

    List<ProductGroupResponse> saveAllImportedProducts(List<ProductGroupResponse> groups);
}
