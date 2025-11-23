package com.spring.fit.backend.chatbot.service;

import com.spring.fit.backend.chatbot.constants.ChatbotConstants;
import com.spring.fit.backend.category.domain.entity.Category;
import com.spring.fit.backend.category.repository.CategoryRepository;
import com.spring.fit.backend.product.domain.entity.*;
import com.spring.fit.backend.product.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(
        name = "chatbot.ingestion.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@org.springframework.core.annotation.Order(2) 
public class ProductIngestionService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductIngestionService.class);

    private final VectorStore vectorStore;
    private final ProductMainRepository productMainRepository;
    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final JdbcTemplate jdbcTemplate;

    @Value("${chatbot.ingestion.force:false}")
    private boolean forceIngestion;

    public ProductIngestionService(
            VectorStore vectorStore,
            ProductMainRepository productMainRepository,
            CategoryRepository categoryRepository,
            ColorRepository colorRepository,
            SizeRepository sizeRepository,
            JdbcTemplate jdbcTemplate) {
        this.vectorStore = vectorStore;
        this.productMainRepository = productMainRepository;
        this.categoryRepository = categoryRepository;
        this.colorRepository = colorRepository;
        this.sizeRepository = sizeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if ingestion is needed
        if (!shouldIngest()) {
            log.info("Vector store already contains data. Skipping ingestion. Set chatbot.ingestion.force=true to force re-ingestion.");
            return;
        }

        ingestProducts();
    }

    private boolean shouldIngest() {
        try {
            // Check if vector_store table exists and has data
            String checkTableQuery = String.format("""
                SELECT EXISTS (
                    SELECT 1 
                    FROM information_schema.tables 
                    WHERE table_schema = '%s' 
                    AND table_name = '%s'
                )
                """, ChatbotConstants.SCHEMA_PUBLIC, ChatbotConstants.TABLE_VECTOR_STORE);
            
            Boolean tableExists = jdbcTemplate.queryForObject(checkTableQuery, Boolean.class);
            
            if (Boolean.FALSE.equals(tableExists)) {
                log.info("vector_store table does not exist. Ingestion will proceed.");
                return true;
            }

            // Check document count
            String countQuery = "SELECT COUNT(*) FROM " + ChatbotConstants.TABLE_VECTOR_STORE;
            Long documentCount = jdbcTemplate.queryForObject(countQuery, Long.class);
            
            if (documentCount == null || documentCount == 0) {
                log.info("Vector store is empty. Ingestion will proceed.");
                return true;
            }

            if (forceIngestion) {
                log.info("Force ingestion is enabled. Will re-ingest {} existing documents.", documentCount);
                // Clear existing data if force is enabled
                clearVectorStore();
                return true;
            }

            log.info("Vector store already contains {} documents. Ingestion skipped.", documentCount);
            return false;
            
        } catch (Exception e) {
            log.error("Error checking vector store status: {}", e.getMessage(), e);
            // If check fails, proceed with ingestion to be safe
            log.warn("Proceeding with ingestion due to check error.");
            return true;
        }
    }

    /**
     * Clear all documents from vector store
     */
    private void clearVectorStore() {
        try {
            log.info("Clearing existing documents from vector store...");
            jdbcTemplate.execute(ChatbotConstants.SQL_TRUNCATE_VECTOR_STORE);
            log.info("Vector store cleared successfully.");
        } catch (Exception e) {
            log.error("Error clearing vector store: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to clear vector store", e);
        }
    }

    /**
     * Force ingestion by clearing existing data first
     */
    public void forceIngest() throws Exception {
        log.info("Inside ProductIngestionService.forceIngest method to force ingestion...");
        clearVectorStore();
        ingestProducts();
    }

    public void ingestProducts() throws Exception {
        log.info("Inside ProductIngestionService.ingestProducts method to ingest products...");

        List<Document> documents = new ArrayList<>();

        // Ingest all active products with their complete information
        List<Product> products = productMainRepository.findAllActiveProductsWithDetails();

        log.info("Inside ProductIngestionService.ingestProducts method to ingest products... Found {} active products to ingest", products.size());

        for (Product product : products) {
            try {
                // Ingest product-level document
                String productDocument = buildProductDocument(product);
                if (productDocument != null && !productDocument.trim().isEmpty()) {
                    Document doc = new Document(productDocument);
                    doc.getMetadata().put(ChatbotConstants.METADATA_TYPE, ChatbotConstants.DOC_TYPE_PRODUCT);
                    doc.getMetadata().put(ChatbotConstants.METADATA_PRODUCT_ID, product.getId().toString());
                    doc.getMetadata().put(ChatbotConstants.METADATA_TITLE, product.getTitle());
                    documents.add(doc);
                }
                
                // Ingest each ProductDetail separately with productDetailId
                if (product.getDetails() != null) {
                    List<ProductDetail> activeDetails = product.getDetails().stream()
                            .filter(detail -> detail.getIsActive() != null && detail.getIsActive())
                            .collect(Collectors.toList());
                    
                    for (ProductDetail detail : activeDetails) {
                        try {
                            String detailDocument = buildProductDetailDocument(product, detail);
                            if (detailDocument != null && !detailDocument.trim().isEmpty()) {
                                Document detailDoc = new Document(detailDocument);
                                detailDoc.getMetadata().put(ChatbotConstants.METADATA_TYPE, ChatbotConstants.DOC_TYPE_PRODUCT_DETAIL);
                                detailDoc.getMetadata().put(ChatbotConstants.METADATA_PRODUCT_ID, product.getId().toString());
                                detailDoc.getMetadata().put(ChatbotConstants.METADATA_PRODUCT_DETAIL_ID, detail.getId().toString());
                                detailDoc.getMetadata().put(ChatbotConstants.METADATA_TITLE, product.getTitle());
                                if (detail.getColor() != null) {
                                    detailDoc.getMetadata().put(ChatbotConstants.METADATA_COLOR, detail.getColor().getName());
                                    detailDoc.getMetadata().put(ChatbotConstants.METADATA_COLOR_ID, detail.getColor().getId().toString());
                                }
                                if (detail.getSize() != null) {
                                    detailDoc.getMetadata().put(ChatbotConstants.METADATA_SIZE, detail.getSize().getLabel());
                                    detailDoc.getMetadata().put(ChatbotConstants.METADATA_SIZE_CODE, detail.getSize().getCode());
                                    detailDoc.getMetadata().put(ChatbotConstants.METADATA_SIZE_ID, detail.getSize().getId().toString());
                                }
                                if (detail.getPrice() != null) {
                                    detailDoc.getMetadata().put(ChatbotConstants.METADATA_PRICE, detail.getPrice().toString());
                                }
                                if (detail.getQuantity() != null) {
                                    detailDoc.getMetadata().put(ChatbotConstants.METADATA_QUANTITY, detail.getQuantity().toString());
                                }
                                
                                // Add first image URL to metadata for easy access
                                if (detail.getProductImages() != null && !detail.getProductImages().isEmpty()) {
                                    String firstImageUrl = detail.getProductImages().stream()
                                            .filter(pi -> pi.getImage() != null && pi.getImage().getUrl() != null)
                                            .map(pi -> pi.getImage().getUrl())
                                            .findFirst()
                                            .orElse("");
                                    if (!firstImageUrl.isEmpty()) {
                                        detailDoc.getMetadata().put(ChatbotConstants.METADATA_IMAGE_URL, firstImageUrl);
                                        
                                        // Also add all image URLs as comma-separated string
                                        String allImageUrls = detail.getProductImages().stream()
                                                .filter(pi -> pi.getImage() != null && pi.getImage().getUrl() != null)
                                                .map(pi -> pi.getImage().getUrl())
                                                .limit(ChatbotConstants.MAX_IMAGES_PER_DETAIL)
                                                .collect(Collectors.joining(ChatbotConstants.DELIMITER_COMMA_SPACE));
                                        if (!allImageUrls.isEmpty()) {
                                            detailDoc.getMetadata().put(ChatbotConstants.METADATA_IMAGE_URLS, allImageUrls);
                                        }
                                    }
                                }
                                
                                // Add category information to metadata for outfit recommendation
                                if (product.getCategories() != null && !product.getCategories().isEmpty()) {
                                    String categoryNames = product.getCategories().stream()
                                            .filter(Category::getIsActive)
                                            .map(Category::getName)
                                            .collect(Collectors.joining(ChatbotConstants.DELIMITER_COMMA_SPACE));
                                    if (!categoryNames.isEmpty()) {
                                        detailDoc.getMetadata().put(ChatbotConstants.METADATA_CATEGORIES, categoryNames);
                                        
                                        // Add category slugs for easier matching
                                        String categorySlugs = product.getCategories().stream()
                                                .filter(Category::getIsActive)
                                                .map(Category::getSlug)
                                                .collect(Collectors.joining(ChatbotConstants.DELIMITER_COMMA_SPACE));
                                        if (!categorySlugs.isEmpty()) {
                                            detailDoc.getMetadata().put(ChatbotConstants.METADATA_CATEGORY_SLUGS, categorySlugs);
                                        }
                                        
                                        // Add parent category names for product type identification
                                        String parentCategoryNames = product.getCategories().stream()
                                                .filter(Category::getIsActive)
                                                .filter(cat -> cat.getParent() != null)
                                                .map(cat -> cat.getParent().getName())
                                                .distinct()
                                                .collect(Collectors.joining(ChatbotConstants.DELIMITER_COMMA_SPACE));
                                        if (!parentCategoryNames.isEmpty()) {
                                            detailDoc.getMetadata().put(ChatbotConstants.METADATA_PARENT_CATEGORIES, parentCategoryNames);
                                        }
                                    }
                                }
                                
                                documents.add(detailDoc);
                            }
                        } catch (Exception e) {
                            log.error("Error processing productDetail ID {}: {}", detail.getId(), e.getMessage(), e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error processing product ID {}: {}", product.getId(), e.getMessage(), e);
            }
        }

        // Ingest category information
        // Using fetch join to avoid LazyInitializationException
        List<Category> categories = categoryRepository.findAllActiveCategoriesWithRelations();

        log.info("Found {} active categories to ingest", categories.size());

        for (Category category : categories) {
            try {
                String categoryDocument = buildCategoryDocument(category);
                if (categoryDocument != null && !categoryDocument.trim().isEmpty()) {
                    Document doc = new Document(categoryDocument);
                    doc.getMetadata().put(ChatbotConstants.METADATA_TYPE, ChatbotConstants.DOC_TYPE_CATEGORY);
                    doc.getMetadata().put(ChatbotConstants.METADATA_CATEGORY_ID, category.getId().toString());
                    doc.getMetadata().put(ChatbotConstants.METADATA_NAME, category.getName());
                    doc.getMetadata().put(ChatbotConstants.METADATA_SLUG, category.getSlug());
                    documents.add(doc);
                }
            } catch (Exception e) {
                log.error("Error processing category ID {}: {}", category.getId(), e.getMessage(), e);
            }
        }

        // Ingest color and size information
        List<Color> colors = colorRepository.findAllActiveColors();
        log.info("Found {} active colors to ingest", colors.size());

        for (Color color : colors) {
            try {
                String colorDocument = buildColorDocument(color);
                if (colorDocument != null && !colorDocument.trim().isEmpty()) {
                    Document doc = new Document(colorDocument);
                    doc.getMetadata().put(ChatbotConstants.METADATA_TYPE, ChatbotConstants.DOC_TYPE_COLOR);
                    doc.getMetadata().put(ChatbotConstants.METADATA_COLOR_ID, color.getId().toString());
                    doc.getMetadata().put(ChatbotConstants.METADATA_NAME, color.getName());
                    documents.add(doc);
                }
            } catch (Exception e) {
                log.error("Error processing color ID {}: {}", color.getId(), e.getMessage(), e);
            }
        }

        List<Size> sizes = sizeRepository.findAllActiveSizes();
        log.info("Found {} active sizes to ingest", sizes.size());

        for (Size size : sizes) {
            try {
                String sizeDocument = buildSizeDocument(size);
                if (sizeDocument != null && !sizeDocument.trim().isEmpty()) {
                    Document doc = new Document(sizeDocument);
                    doc.getMetadata().put(ChatbotConstants.METADATA_TYPE, ChatbotConstants.DOC_TYPE_SIZE);
                    doc.getMetadata().put(ChatbotConstants.METADATA_SIZE_ID, size.getId().toString());
                    doc.getMetadata().put(ChatbotConstants.METADATA_CODE, size.getCode());
                    documents.add(doc);
                }
            } catch (Exception e) {
                log.error("Error processing size ID {}: {}", size.getId(), e.getMessage(), e);
            }
        }

        // Split documents into smaller chunks if needed
        TextSplitter textSplitter = new TokenTextSplitter();
        List<Document> splitDocuments = new ArrayList<>();
        for (Document doc : documents) {
            splitDocuments.addAll(textSplitter.apply(List.of(doc)));
        }

        // Add all documents to vector store (outside of read-only transaction)
        if (!splitDocuments.isEmpty()) {
            // Note: vectorStore.accept() needs write access, so this must be outside @Transactional(readOnly = true)
            vectorStore.accept(splitDocuments);
            log.info("Successfully ingested {} documents into vector store", splitDocuments.size());
        } else {
            log.warn("No documents to ingest!");
        }

        log.info("Product data ingestion completed!");
    }

    private String buildProductDocument(Product product) {
        StringBuilder sb = new StringBuilder();

        sb.append(ChatbotConstants.DOC_PRODUCT_INFO).append("\n");
        sb.append(ChatbotConstants.DOC_FIELD_ID).append(product.getId()).append("\n");
        sb.append(ChatbotConstants.DOC_FIELD_TITLE).append(product.getTitle()).append("\n");
        if (product.getDescription() != null && !product.getDescription().trim().isEmpty()) {
            sb.append(ChatbotConstants.DOC_FIELD_DESCRIPTION).append(product.getDescription()).append("\n");
        }

        // Categories
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            sb.append(ChatbotConstants.DOC_FIELD_CATEGORIES);
            String categoryNames = product.getCategories().stream()
                    .filter(Category::getIsActive)
                    .map(Category::getName)
                    .collect(Collectors.joining(ChatbotConstants.DELIMITER_COMMA_SPACE));
            sb.append(categoryNames).append("\n");
        }

        // Product Details (variants)
        if (product.getDetails() != null) {
            List<ProductDetail> activeDetails = product.getDetails().stream()
                    .filter(detail -> detail.getIsActive() != null && detail.getIsActive())
                    .collect(Collectors.toList());

            if (!activeDetails.isEmpty()) {
                sb.append("\n").append(ChatbotConstants.DOC_FIELD_VARIANTS).append("\n");
                for (ProductDetail detail : activeDetails) {
                    sb.append("- ");
                    if (detail.getColor() != null) {
                        sb.append(ChatbotConstants.DOC_FIELD_COLOR).append(detail.getColor().getName())
                                .append(ChatbotConstants.DELIMITER_COMMA_SPACE);
                    }
                    if (detail.getSize() != null) {
                        sb.append(ChatbotConstants.DOC_FIELD_SIZE).append(detail.getSize().getLabel())
                                .append(" (").append(detail.getSize().getCode()).append(")")
                                .append(ChatbotConstants.DELIMITER_COMMA_SPACE);
                    }
                    if (detail.getPrice() != null) {
                        sb.append(ChatbotConstants.DOC_FIELD_PRICE).append(formatPrice(detail.getPrice()))
                                .append(ChatbotConstants.DELIMITER_COMMA_SPACE);
                    }
                    if (detail.getQuantity() != null) {
                        sb.append(ChatbotConstants.DOC_FIELD_STOCK).append(detail.getQuantity())
                                .append(ChatbotConstants.DOC_FIELD_UNITS);
                    }
                    sb.append("\n");

                    // Image URLs
                    if (detail.getProductImages() != null && !detail.getProductImages().isEmpty()) {
                        String imageUrls = detail.getProductImages().stream()
                                .filter(pi -> pi.getImage() != null && pi.getImage().getUrl() != null)
                                .map(pi -> pi.getImage().getUrl())
                                .limit(ChatbotConstants.MAX_IMAGES_PER_DETAIL)
                                .collect(Collectors.joining(ChatbotConstants.DELIMITER_COMMA_SPACE));
                        if (!imageUrls.isEmpty()) {
                            sb.append("  ").append(ChatbotConstants.DOC_FIELD_IMAGES).append(imageUrls).append("\n");
                        }
                    }
                }
            }
        }

        return sb.toString();
    }

    private String buildCategoryDocument(Category category) {
        StringBuilder sb = new StringBuilder();

        sb.append(ChatbotConstants.DOC_CATEGORY_INFO).append("\n");
        sb.append(ChatbotConstants.DOC_FIELD_ID).append(category.getId()).append("\n");
        sb.append(ChatbotConstants.DOC_FIELD_NAME).append(category.getName()).append("\n");
        sb.append(ChatbotConstants.DOC_FIELD_SLUG).append(category.getSlug()).append("\n");

        if (category.getParent() != null) {
            sb.append(ChatbotConstants.DOC_FIELD_PARENT_CATEGORY).append(category.getParent().getName()).append("\n");
        }

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            String childNames = category.getChildren().stream()
                    .filter(Category::getIsActive)
                    .map(Category::getName)
                    .collect(Collectors.joining(ChatbotConstants.DELIMITER_COMMA_SPACE));
            if (!childNames.isEmpty()) {
                sb.append(ChatbotConstants.DOC_FIELD_SUBCATEGORIES).append(childNames).append("\n");
            }
        }

        return sb.toString();
    }

    private String buildColorDocument(Color color) {
        StringBuilder sb = new StringBuilder();

        sb.append(ChatbotConstants.DOC_COLOR_INFO).append("\n");
        sb.append(ChatbotConstants.DOC_FIELD_ID).append(color.getId()).append("\n");
        sb.append(ChatbotConstants.DOC_FIELD_NAME).append(color.getName()).append("\n");
        if (color.getHex() != null && !color.getHex().trim().isEmpty()) {
            sb.append(ChatbotConstants.DOC_FIELD_HEX_CODE).append(color.getHex()).append("\n");
        }

        return sb.toString();
    }

    private String buildSizeDocument(Size size) {
        StringBuilder sb = new StringBuilder();

        sb.append(ChatbotConstants.DOC_SIZE_INFO).append("\n");
        sb.append(ChatbotConstants.DOC_FIELD_ID).append(size.getId()).append("\n");
        sb.append(ChatbotConstants.DOC_FIELD_CODE).append(size.getCode()).append("\n");
        sb.append(ChatbotConstants.DOC_FIELD_LABEL).append(size.getLabel()).append("\n");

        return sb.toString();
    }

    private String buildProductDetailDocument(Product product, ProductDetail detail) {
        StringBuilder sb = new StringBuilder();

        sb.append(ChatbotConstants.DOC_PRODUCT_DETAIL_INFO).append("\n");
        sb.append(ChatbotConstants.DOC_FIELD_PRODUCT_ID).append(product.getId()).append("\n");
        sb.append(ChatbotConstants.DOC_FIELD_PRODUCT_DETAIL_ID).append(detail.getId()).append("\n");
        sb.append(ChatbotConstants.DOC_FIELD_PRODUCT_TITLE).append(product.getTitle()).append("\n");
        if (product.getDescription() != null && !product.getDescription().trim().isEmpty()) {
            sb.append(ChatbotConstants.DOC_FIELD_DESCRIPTION).append(product.getDescription()).append("\n");
        }

        // Color
        if (detail.getColor() != null) {
            sb.append(ChatbotConstants.DOC_FIELD_COLOR).append(detail.getColor().getName());
            if (detail.getColor().getHex() != null) {
                sb.append(" (").append(detail.getColor().getHex()).append(")");
            }
            sb.append("\n");
        }

        // Size
        if (detail.getSize() != null) {
            sb.append(ChatbotConstants.DOC_FIELD_SIZE).append(detail.getSize().getLabel())
                    .append(" (").append(detail.getSize().getCode()).append(")\n");
        }

        // Price
        if (detail.getPrice() != null) {
            sb.append(ChatbotConstants.DOC_FIELD_PRICE).append(formatPrice(detail.getPrice())).append("\n");
        }

        // Quantity
        if (detail.getQuantity() != null) {
            sb.append(ChatbotConstants.DOC_FIELD_STOCK).append(detail.getQuantity())
                    .append(ChatbotConstants.DOC_FIELD_UNITS).append("\n");
        }

        // Categories
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            sb.append(ChatbotConstants.DOC_FIELD_CATEGORIES);
            String categoryNames = product.getCategories().stream()
                    .filter(Category::getIsActive)
                    .map(Category::getName)
                    .collect(Collectors.joining(ChatbotConstants.DELIMITER_COMMA_SPACE));
            sb.append(categoryNames).append("\n");
        }

        // Image URLs
        if (detail.getProductImages() != null && !detail.getProductImages().isEmpty()) {
            String imageUrls = detail.getProductImages().stream()
                    .filter(pi -> pi.getImage() != null && pi.getImage().getUrl() != null)
                    .map(pi -> pi.getImage().getUrl())
                    .limit(ChatbotConstants.MAX_IMAGES_PER_DETAIL)
                    .collect(Collectors.joining(ChatbotConstants.DELIMITER_COMMA_SPACE));
            if (!imageUrls.isEmpty()) {
                sb.append(ChatbotConstants.DOC_FIELD_IMAGES).append(imageUrls).append("\n");
            }
        }

        return sb.toString();
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return ChatbotConstants.DOC_FIELD_NA;
        }
        return price.toString() + ChatbotConstants.DOC_FIELD_VND;
    }
}

