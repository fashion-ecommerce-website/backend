package com.spring.fit.backend.product.service;

import com.spring.fit.backend.product.domain.dto.request.CreateSizeRequest;
import com.spring.fit.backend.product.domain.dto.request.UpdateSizeRequest;
import com.spring.fit.backend.product.domain.dto.response.SizeResponse;

import java.util.List;

public interface SizeService {
    SizeResponse createSize(CreateSizeRequest request);
    SizeResponse updateSize(Short id, UpdateSizeRequest request);
    List<SizeResponse> getAllSizes();
    List<SizeResponse> getAllActiveSizes();
    void toggleSizeStatus(Short id);
}
