package com.spring.fit.backend.product.service.impl;

import com.spring.fit.backend.category.domain.entity.Category;
import com.spring.fit.backend.category.repository.CategoryRepository;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.product.domain.dto.response.ProductDetailPreviewResponse;

import com.spring.fit.backend.product.domain.dto.response.ProductGroupResponse;
import com.spring.fit.backend.product.domain.entity.*;
import com.spring.fit.backend.product.repository.*;
import com.spring.fit.backend.product.service.ProductImportService;
import com.spring.fit.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImportServiceImpl implements ProductImportService {

    private final ProductMainRepository productMainRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductImageRepository productImageRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final CategoryRepository categoryRepository;
    private final ProductServiceImpl productServiceImpl;
    private final ImageRepository imageRepository;


    @Override
    public List<ProductGroupResponse> parseCsv(MultipartFile file) {
        Map<String, ProductGroupResponse> groupedProducts = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader,
                     CSVFormat.DEFAULT
                             .withDelimiter(';')
                             .withFirstRecordAsHeader()
                             .withIgnoreHeaderCase()
                             .withTrim()
             )) {

            for (CSVRecord record : csvParser) {

                String productTitle = record.get("Product Title");
                String description = record.get("Description");
                String categoryName = record.get("Category");
                String colorName = record.get("Color");
                String images = record.get("IMG");
                String sizeCode = record.get("Size");
                Integer quantity = Integer.valueOf(record.get("Quantity"));
                BigDecimal price = new BigDecimal(record.get("Price"));

                List<String> imageUrls = new ArrayList<>();
                if (images != null && !images.isBlank()) {
                    imageUrls = Arrays.asList(images.split("\\|"));
                }

                ProductDetailPreviewResponse detail = new ProductDetailPreviewResponse(
                        colorName,
                        imageUrls,
                        sizeCode,
                        quantity,
                        price
                );

                // ================================
                // Validate Category
                // ================================
                if (categoryRepository.findByNameIgnoreCaseAndIsActive(categoryName, true).isEmpty()) {
                    detail.setError(true);
                    detail.setErrorMessage("Category not found or inactive: " + categoryName);
                }

                // ================================
                // Validate Color
                // ================================
                boolean colorExists = colorRepository.findByNameIgnoreCase(colorName).isPresent();
                if (!colorExists) {
                    detail.setError(true);
                    String prevMsg = detail.getErrorMessage() != null ? detail.getErrorMessage() + "; " : "";
                    detail.setErrorMessage(prevMsg + "Color not found: " + colorName);
                }

                // ================================
                // Validate Size
                // ================================
                boolean sizeExists = sizeRepository.findByCodeIgnoreCase(sizeCode).isPresent();
                if (!sizeExists) {
                    detail.setError(true);
                    String prevMsg = detail.getErrorMessage() != null ? detail.getErrorMessage() + "; " : "";
                    detail.setErrorMessage(prevMsg + "Size not found: " + sizeCode);
                }

                // ================================
                // Validate unique Product (title + desc + category)
                // ================================
                boolean productExists = false;
                if (categoryRepository.findByNameIgnoreCaseAndIsActive(categoryName, true).isPresent()) {
                    Optional<Category> categoryOpt = categoryRepository.findByNameIgnoreCaseAndIsActive(categoryName, true);
                    if (categoryOpt.isPresent()) {
                        productExists = productMainRepository
                                .existsByTitleIgnoreCaseAndDescriptionIgnoreCaseAndCategoriesIn(
                                        productTitle, description, Set.of(categoryOpt.get()));
                    }
                }
                if (productExists) {
                    detail.setError(true);
                    String prevMsg = detail.getErrorMessage() != null ? detail.getErrorMessage() + "; " : "";
                    detail.setErrorMessage(prevMsg + "Product with same title, description, and category already exists");
                }

                // ================================
                // Grouping product
                // ================================
                String key = productTitle + "::" + description + "::" + categoryName;
                groupedProducts.putIfAbsent(key, new ProductGroupResponse(productTitle, description, categoryName));
                groupedProducts.get(key).addDetail(detail);
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file CSV: " + e.getMessage(), e);
        }

        return new ArrayList<>(groupedProducts.values());
    }

    @Override
    @Transactional
    public void saveAllImportedProducts(List<ProductGroupResponse> groups) {

        for (ProductGroupResponse group : groups) {

            log.info("⏳ Saving product: {}", group.getProductTitle());

            // ================================
            // 1️⃣ Lấy Category
            // ================================
            Category category = categoryRepository
                    .findByNameIgnoreCaseAndIsActive(group.getCategory(), true)
                    .orElseThrow(() ->
                            new ErrorException(HttpStatus.NOT_FOUND,
                                    "Category not found: " + group.getCategory()));

            Set<Category> categories = new HashSet<>();
            categories.add(category);

            // ================================
            // 2️⃣ Tạo Product
            // ================================
            Product product = new Product();
            product.setTitle(group.getProductTitle());
            product.setDescription(group.getDescription());
            product.setCategories(categories);
            product.setIsActive(true);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());

            // Khởi tạo collection details để tránh null
            product.setDetails(new HashSet<>());

            product = productMainRepository.save(product); // lưu trước để có ID

            // ================================
            // 3️⃣ Lưu ProductDetail + Image
            // ================================
            for (ProductDetailPreviewResponse preview : group.getProductDetails()) {

                // ---- Lấy Color ----
                Color color = colorRepository
                        .findByNameIgnoreCase(preview.getColor())
                        .orElseThrow(() ->
                                new ErrorException(HttpStatus.NOT_FOUND,
                                        "Color not found: " + preview.getColor()));

                // ---- Lấy Size ----
                Size size = sizeRepository
                        .findByCodeIgnoreCase(preview.getSize())
                        .orElseThrow(() ->
                                new ErrorException(HttpStatus.NOT_FOUND,
                                        "Size not found: " + preview.getSize()));

                // ---- Kiểm tra tồn tại Variant ----
                if (productDetailRepository.findByActiveProductAndColorAndSize(
                        product.getId(), color.getId(), size.getId()).isPresent()) {

                    throw new ErrorException(HttpStatus.CONFLICT,
                            "Variant already exists: " + color.getName() + " - " + size.getCode());
                }

                // ---- Tạo ProductDetail ----
                ProductDetail detail = new ProductDetail();
                detail.setProduct(product);
                detail.setColor(color);
                detail.setSize(size);
                detail.setQuantity(preview.getQuantity());
                detail.setPrice(preview.getPrice());
                detail.setIsActive(true);
                detail.setCreatedAt(LocalDateTime.now());
                detail.setUpdatedAt(LocalDateTime.now());
                detail.setSlug(productServiceImpl.generateSlug(
                        product.getTitle(),
                        color.getName(),
                        size.getCode()
                ));

                // Thêm vào collection của Product ngay lập tức
                product.getDetails().add(detail);

                // Lưu ProductDetail
                detail = productDetailRepository.save(detail);

                // ---- Lưu ProductImage ----
                if (preview.getImageUrls() != null) {
                    for (String url : preview.getImageUrls()) {
                        Image image = new Image();
                        image.setUrl(url);
                        image.setAlt(detail.getColor().getName() + " " + detail.getSize().getCode());
                        image = imageRepository.save(image);

                        ProductImage productImage = new ProductImage();
                        productImage.setDetail(detail);
                        productImage.setImage(image);
                        productImageRepository.save(productImage);
                    }
                }
            }

            // Cập nhật product (tùy chọn, Hibernate có thể tự quản lý)
            productMainRepository.save(product);

            log.info("✅ Saved product success: {}", product.getTitle());
        }
    }

}