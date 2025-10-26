package com.spring.fit.backend.product.service.impl;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.product.domain.dto.request.ColorRequest;
import com.spring.fit.backend.product.domain.dto.response.ColorResponse;
import com.spring.fit.backend.product.domain.entity.Color;
import com.spring.fit.backend.product.repository.ColorRepository;
import com.spring.fit.backend.product.service.ColorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColorServiceImpl implements ColorService {

    private final ColorRepository colorRepository;

    @Override
    @Transactional
    public ColorResponse createColor(ColorRequest request) {
        // 🔹 Validate trùng tên
        if (colorRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Color name already exists");
        }

        // 🔹 Validate mã HEX (nếu có)
        if (request.getHex() != null && !request.getHex().matches("^#[0-9A-Fa-f]{6}$")) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Invalid HEX color format");
        }

        Color color = new Color();
        color.setName(request.getName().trim());
        color.setHex(request.getHex());
        color.setIsActive(true);

        colorRepository.save(color);
        return mapToResponse(color);
    }

    @Override
    @Transactional
    public ColorResponse updateColor(Short id, ColorRequest request) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Color not found"));

        Optional<Color> existing = colorRepository.findByNameIgnoreCase(request.getName());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Color name already exists");
        }

        // 🔹 Validate HEX code
        if (request.getHex() != null && !request.getHex().matches("^#[0-9A-Fa-f]{6}$")) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Invalid HEX color format");
        }

        color.setName(request.getName().trim());
        color.setHex(request.getHex());

        colorRepository.save(color);
        return mapToResponse(color);
    }


    @Override
    public List<ColorResponse> getAllColors() {
        return colorRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ColorResponse> getAllActiveColors() {
        return colorRepository.findAllActiveColors()
                .stream()
                .map(c -> new ColorResponse(c.getId(), c.getName(), c.getHex(), c.getIsActive()))
                .collect(Collectors.toList());
    }

    @Override
    public void toggleColorStatus(Short id) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Color not found"));

        if (color.getIsActive()) {
            boolean existsActiveProduct = colorRepository.existsActiveProductDetailWithQuantity(id);
            if (existsActiveProduct) {
                throw new ErrorException(HttpStatus.BAD_REQUEST,
                        "Cannot deactivate this color because it's used by active product details with quantity > 0");
            }

            color.setIsActive(false);
        } else {
            // Reactivate
            color.setIsActive(true);
        }

        colorRepository.save(color);
    }
    private ColorResponse mapToResponse(Color color) {
        return new ColorResponse(color.getId(), color.getName(), color.getHex(), color.getIsActive());
    }
}