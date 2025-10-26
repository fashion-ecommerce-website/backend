package com.spring.fit.backend.product.service.impl;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.product.domain.dto.request.SizeRequest;
import com.spring.fit.backend.product.domain.dto.response.SizeResponse;
import com.spring.fit.backend.product.domain.entity.Size;
import com.spring.fit.backend.product.repository.SizeRepository;
import com.spring.fit.backend.product.repository.ProductDetailRepository;
import com.spring.fit.backend.product.service.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class SizeServiceImpl implements SizeService {

    private final SizeRepository sizeRepository;
    private final ProductDetailRepository productDetailRepository;

    @Override
    @Transactional
    public SizeResponse createSize(SizeRequest request) {
        if (sizeRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Size code already exists");
        }

        Size size = new Size();
        size.setCode(request.getCode().trim());
        size.setLabel(request.getLabel().trim());
        size.setIsActive(true);

        sizeRepository.save(size);
        return mapToResponse(size);
    }

    @Override
    @Transactional
    public SizeResponse updateSize(Short id, SizeRequest request) {
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Size not found"));

        Optional<Size> existing = sizeRepository.findByCodeIgnoreCase(request.getCode());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Size code already exists");
        }

        size.setCode(request.getCode().trim());
        size.setLabel(request.getLabel().trim());
        sizeRepository.save(size);

        return mapToResponse(size);
    }

    @Override
    public List<SizeResponse> getAllSizes() {
        return sizeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SizeResponse> getAllActiveSizes() {
        return sizeRepository.findAllActiveSizes()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleSizeStatus(Short id) {
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Size not found"));

        if (size.getIsActive()) {
            boolean existsActiveProduct = sizeRepository.existsActiveProductDetailWithQuantity(id);
            if (existsActiveProduct) {
                throw new ErrorException(HttpStatus.BAD_REQUEST,
                        "Cannot deactivate this size because it's used by active product details with quantity > 0");
            }

            size.setIsActive(false);
        } else {
            size.setIsActive(true);
        }

        sizeRepository.save(size);
    }

    private SizeResponse mapToResponse(Size size) {
        return new SizeResponse(size.getId(), size.getCode(), size.getLabel(), size.getIsActive());
    }
}