package com.spring.fit.backend.product.service;

import com.spring.fit.backend.product.domain.dto.request.ColorRequest;
import com.spring.fit.backend.product.domain.dto.response.ColorResponse;

import java.util.List;

public interface ColorService {
    ColorResponse createColor(ColorRequest request);
    ColorResponse updateColor(Short id, ColorRequest request);
    List<ColorResponse> getAllColors();
    void toggleColorStatus(Short id);
    List<ColorResponse> getAllActiveColors();
}
