package com.spring.fit.backend.product.service.impl;

import com.spring.fit.backend.category.domain.entity.Category;
import com.spring.fit.backend.category.repository.CategoryRepository;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.common.service.ImageService;
import com.spring.fit.backend.product.domain.dto.request.ProductDetailCheckRequest;
import com.spring.fit.backend.product.domain.dto.response.ProductDetailCheckResponse;
import com.spring.fit.backend.product.domain.dto.response.ProductDetailPreviewResponse;
import com.spring.fit.backend.product.domain.dto.response.ProductGroupResponse;
import com.spring.fit.backend.product.domain.entity.*;
import com.spring.fit.backend.product.repository.*;
import com.spring.fit.backend.product.service.ProductImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    private final ImageService imageService;

    @Override
    public List<ProductGroupResponse> parseCsvWithZips(MultipartFile csvFile, List<MultipartFile> zipFiles) {
        Map<String, Map<String, List<File>>> zipImageMap = new HashMap<>();
        Map<String, Integer> counterMap = new HashMap<>(); // đếm số ảnh theo title-color

        try {
            // 1️⃣ Duyệt tất cả ZIP
            for (MultipartFile zip : zipFiles) {
                Path tempDir = Files.createTempDirectory("zip-import-");
                log.info("Temp dir created at {}", tempDir.toAbsolutePath());

                unzipMultiple(zip.getInputStream(), tempDir, counterMap);
                Map<String, Map<String, List<File>>> map = buildZipImageMap(tempDir);

                // merge vào zipImageMap
                map.forEach((title, colorMap) ->
                        zipImageMap.merge(title, colorMap, (oldMap, newMap) -> {
                            newMap.forEach((color, files) ->
                                    oldMap.merge(color, files, (oldFiles, newFiles) -> {
                                        oldFiles.addAll(newFiles);
                                        return oldFiles;
                                    })
                            );
                            return oldMap;
                        })
                );
            }

            // 2️⃣ Đọc CSV
            List<CSVRecord> records = readCsv(csvFile);
            log.info("CSV record count: {}", records.size());

            Map<String, ProductGroupResponse> groupedProducts = new HashMap<>();
            Set<String> fileUniqueCheck = new HashSet<>(); // set để check duplicate trong file

            for (CSVRecord r : records) {
                String title = r.get("Product Title").trim();
                String desc = r.get("Description").trim();
                String categoryName = r.get("Category").trim();
                String color = r.get("Color").trim();
                String size = r.get("Size").trim();
                Integer quantity = r.isMapped("Quantity") && !r.get("Quantity").isBlank()
                        ? Integer.valueOf(r.get("Quantity"))
                        : 0;
                BigDecimal price = r.isMapped("Price") && !r.get("Price").isBlank()
                        ? new BigDecimal(r.get("Price"))
                        : BigDecimal.ZERO;

                // 3️⃣ Lấy file từ ZIP map
                List<File> files = Optional.ofNullable(zipImageMap.get(title))
                        .map(m -> m.get(color))
                        .orElse(Collections.emptyList());

                // 4️⃣ Tạo detail
                ProductDetailPreviewResponse detail = new ProductDetailPreviewResponse();
                detail.setColor(color);
                detail.setSize(size);
                detail.setQuantity(quantity);
                detail.setPrice(price);
                detail.setLocalFiles(new ArrayList<>(files));
                detail.setImageUrls(files.stream().map(File::getAbsolutePath).toList());

                // 5️⃣ Validate cơ bản
                validateDetail(detail, categoryName, title, desc);

                // 6️⃣ Validate duplicate trong file
                String uniqueKey = title.toLowerCase() + "::" + color.toLowerCase() + "::" + size.toLowerCase();
                if (fileUniqueCheck.contains(uniqueKey)) {
                    detail.setError(true);
                    String msg = detail.getErrorMessage() != null ? detail.getErrorMessage() + " " : "";
                    detail.setErrorMessage(msg + "Duplicate in CSV/ZIP: " + uniqueKey);
                } else {
                    fileUniqueCheck.add(uniqueKey);
                }

                // 7️⃣ Validate duplicate trong DB
                boolean existsInDb = productDetailRepository.existsByProductTitleAndColorAndSize(title, color, size);
                if (existsInDb) {
                    detail.setError(true);
                    String msg = detail.getErrorMessage() != null ? detail.getErrorMessage() + " " : "";
                    detail.setErrorMessage(msg + "Already exists in DB");
                }

                // 8️⃣ Group
                String key = title + "::" + desc + "::" + categoryName;
                groupedProducts.putIfAbsent(key, new ProductGroupResponse(title, desc, categoryName));
                groupedProducts.get(key).addDetail(detail);
            }

            return new ArrayList<>(groupedProducts.values());

        } catch (Exception ex) {
            throw new RuntimeException("Error parsing CSV + multiple ZIPs: " + ex.getMessage(), ex);
        }
    }


    @Override
    @Transactional
    public List<ProductGroupResponse> saveAllImportedProducts(List<ProductGroupResponse> groups) {
        List<ProductGroupResponse> result = new ArrayList<>();

        for (ProductGroupResponse group : groups) {
            Category category = categoryRepository
                    .findByNameIgnoreCaseAndIsActive(group.getCategory(), true)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND,
                            "Category not found: " + group.getCategory()));

            Product product = new Product();
            product.setTitle(group.getProductTitle());
            product.setDescription(group.getDescription());
            product.setCategories(Set.of(category));
            product.setIsActive(true);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            product = productMainRepository.save(product);

            for (ProductDetailPreviewResponse d : group.getProductDetails()) {
                Color color = colorRepository.findByNameIgnoreCase(d.getColor())
                        .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Color not found: " + d.getColor()));

                Size size = sizeRepository.findByCodeIgnoreCase(d.getSize())
                        .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Size not found: " + d.getSize()));

                ProductDetail detail = new ProductDetail();
                detail.setProduct(product);
                detail.setColor(color);
                detail.setSize(size);
                detail.setQuantity(d.getQuantity());
                detail.setPrice(d.getPrice());
                detail.setIsActive(true);
                detail.setSlug(productServiceImpl.generateSlug(group.getProductTitle(), color.getName(), size.getCode()));
                detail = productDetailRepository.save(detail);

                // 1️⃣ Upload file lên Cloudinary
                List<String> cdnUrls = new ArrayList<>();
                if (d.getLocalFiles() != null) {
                    for (File f : d.getLocalFiles()) {
                        try {
                            String url = imageService.uploadImage(f, "import/" + product.getId());
                            Image img = new Image();
                            img.setUrl(url);
                            img.setAlt(color.getName());
                            img = imageRepository.save(img);

                            ProductImage pi = new ProductImage();
                            pi.setDetail(detail);
                            pi.setImage(img);
                            productImageRepository.save(pi);

                            cdnUrls.add(url);

                        } catch (Exception ex) {
                            log.error("Failed to upload or save image {}: {}", f.getAbsolutePath(), ex.getMessage());
                        }
                    }
                }

                // 2️⃣ Cập nhật imageUrls để FE nhận
                d.setImageUrls(cdnUrls);

            }

            result.add(group);
        }

        for (ProductGroupResponse group : groups) {
            for (ProductDetailPreviewResponse d : group.getProductDetails()) {
                if (d.getLocalFiles() != null && !d.getLocalFiles().isEmpty()) {
                    Path tempDir = d.getLocalFiles().get(0).toPath().getParent(); // Lấy thư mục chứa file
                    safeDelete(tempDir); // xóa toàn bộ thư mục tạm
                    d.getLocalFiles().clear(); // dọn list localFiles
                }
            }
        }

        return result;
    }

    @Override
    public ProductDetailCheckResponse checkProductDetail(ProductDetailCheckRequest request) {
        ProductDetailCheckResponse response = new ProductDetailCheckResponse();
        response.setError(false);

        String key = request.getProductTitle().toLowerCase() + "::" +
                request.getDetail().getColor().toLowerCase() + "::" +
                request.getDetail().getSize().toLowerCase();

        StringBuilder sb = new StringBuilder();

        // 1️⃣ Check duplicate trong file CSV/ZIP
        if (request.getFileProductDetails() != null) {
            long count = request.getFileProductDetails().stream()
                    .filter(d -> d != request.getDetail()) // bỏ qua chính detail đang check
                    .filter(d -> d.getProductTitle().equalsIgnoreCase(request.getProductTitle())
                            && d.getColor().equalsIgnoreCase(request.getDetail().getColor())
                            && d.getSize().equalsIgnoreCase(request.getDetail().getSize()))
                    .count();
            if (count > 0) {
                sb.append("Duplicate in uploaded file: ").append(key).append("; ");
            }
        }

        // 2️⃣ Check tồn tại trong DB
        boolean existsInDb = productDetailRepository.existsByProductTitleAndColorAndSize(
                request.getProductTitle(),
                request.getDetail().getColor(),
                request.getDetail().getSize()
        );
        if (existsInDb) {
            sb.append("ProductDetail already exists in DB: ").append(key).append("; ");
        }

        // 3️⃣ Validate color, size, category
        if (!colorRepository.findByNameIgnoreCase(request.getDetail().getColor()).isPresent()) {
            sb.append("Color not found: ").append(request.getDetail().getColor()).append("; ");
        }

        if (!sizeRepository.findByCodeIgnoreCase(request.getDetail().getSize()).isPresent()) {
            sb.append("Size not found: ").append(request.getDetail().getSize()).append("; ");
        }

        if (!categoryRepository.findByNameIgnoreCaseAndIsActive(request.getDetail().getCategory(),true).isPresent()) {
            sb.append("Category not found for product: ").append(request.getDetail().getCategory()).append("; ");
        }

        // 4️⃣ Set response
        if (sb.length() > 0) {
            response.setError(true);
            response.setErrorMessage(sb.toString());
        }

        return response;
    }


    private void unzipMultiple(InputStream is, Path target, Map<String, Integer> counterMap) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newPath = target.resolve(entry.getName()).normalize();
                if (!newPath.startsWith(target))
                    throw new IOException("Bad zip entry: " + entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());

                    // ===== đổi tên file =====
                    Path rel = target.relativize(newPath);
                    int count = rel.getNameCount();
                    if (count >= 3) {
                        String color = rel.getName(count - 2).toString();
                        String title = rel.getName(count - 3).toString();

                        String counterKey = title + "::" + color;
                        int number = counterMap.getOrDefault(counterKey, 1);

                        String fileName = newPath.getFileName().toString();
                        String ext = "";
                        int dot = fileName.lastIndexOf(".");
                        if (dot != -1) ext = fileName.substring(dot);

                        String safeTitle = title.replaceAll("[^a-zA-Z0-9]", "_");
                        String safeColor = color.replaceAll("[^a-zA-Z0-9]", "_");

                        String newFileName = safeTitle + "-" + safeColor + "-" + number + ext;
                        newPath = newPath.getParent().resolve(newFileName);

                        counterMap.put(counterKey, number + 1);
                    }
                    // ======================

                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }


    private Map<String, Map<String, List<File>>> buildZipImageMap(Path root) throws IOException {
        Map<String, Map<String, List<File>>> map = new HashMap<>();

        Files.walk(root)
                .filter(Files::isRegularFile)
                .forEach(path -> {

                    Path rel = root.relativize(path);
                    int count = rel.getNameCount();

                    if (count < 3) return; String color = rel.getName(count - 2).toString(); String title = rel.getName(count - 3).toString();
                    map.computeIfAbsent(title, k -> new HashMap<>());
                    map.get(title).computeIfAbsent(color, k -> new ArrayList<>());
                    map.get(title).get(color).add(path.toFile());
                });

        return map;
    }

    private List<CSVRecord> readCsv(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            CSVParser parser = CSVFormat.DEFAULT
                    .withDelimiter(';')
                    .withFirstRecordAsHeader()
                    .parse(reader);

            return parser.getRecords();
        }
    }

    private void validateDetail(ProductDetailPreviewResponse d,
                                String category,
                                String title,
                                String desc) {

        boolean error = false;
        StringBuilder sb = new StringBuilder();

        if (categoryRepository.findByNameIgnoreCaseAndIsActive(category, true).isEmpty()) {
            error = true;
            sb.append("Category not found: ").append(category).append("; ");
        }
        if (colorRepository.findByNameIgnoreCase(d.getColor()).isEmpty()) {
            error = true;
            sb.append("Color not found: ").append(d.getColor()).append("; ");
        }
        if (sizeRepository.findByCodeIgnoreCase(d.getSize()).isEmpty()) {
            error = true;
            sb.append("Size not found: ").append(d.getSize()).append("; ");
        }

        d.setError(error);
        d.setErrorMessage(sb.length() > 0 ? sb.toString() : null);
    }

    private void safeDelete(Path dir) {
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (Exception ignored) {}
    }
}
