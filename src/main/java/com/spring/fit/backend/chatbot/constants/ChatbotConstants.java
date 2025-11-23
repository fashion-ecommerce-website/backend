package com.spring.fit.backend.chatbot.constants;

/**
 * Constants for chatbot functionality
 */
public final class ChatbotConstants {

    private ChatbotConstants() {
        // Utility class - prevent instantiation
    }

    // ==================== Response Types ====================
    public static final String RESPONSE_TYPE_PRODUCT = "PRODUCT";
    public static final String RESPONSE_TYPE_MESSAGE = "MESSAGE";

    // ==================== Error Messages ====================
    public static final String ERROR_MESSAGE_EMPTY = "Message cannot be empty";
    public static final String ERROR_PROCESSING_CHAT = "Error processing chat: ";
    public static final String ERROR_REINGESTION = "Error during re-ingestion: ";

    // ==================== Outfit Query Keywords ====================
    public static final String[] OUTFIT_QUERY_KEYWORDS = {
            "outfit", "phối", "styling", "bộ", "combo", "set"
    };

    // ==================== Metadata Keys ====================
    public static final String METADATA_TYPE = "type";
    public static final String METADATA_PRODUCT_ID = "productId";
    public static final String METADATA_PRODUCT_DETAIL_ID = "productDetailId";
    public static final String METADATA_TITLE = "title";
    public static final String METADATA_COLOR = "color";
    public static final String METADATA_COLOR_ID = "colorId";
    public static final String METADATA_SIZE = "size";
    public static final String METADATA_SIZE_CODE = "sizeCode";
    public static final String METADATA_SIZE_ID = "sizeId";
    public static final String METADATA_PRICE = "price";
    public static final String METADATA_QUANTITY = "quantity";
    public static final String METADATA_IMAGE_URL = "imageUrl";
    public static final String METADATA_IMAGE_URLS = "imageUrls";
    public static final String METADATA_CATEGORIES = "categories";
    public static final String METADATA_CATEGORY_SLUGS = "categorySlugs";
    public static final String METADATA_PARENT_CATEGORIES = "parentCategories";
    public static final String METADATA_CATEGORY_ID = "categoryId";
    public static final String METADATA_NAME = "name";
    public static final String METADATA_SLUG = "slug";
    public static final String METADATA_CODE = "code";

    // ==================== Document Types ====================
    public static final String DOC_TYPE_PRODUCT = "product";
    public static final String DOC_TYPE_PRODUCT_DETAIL = "productDetail";
    public static final String DOC_TYPE_CATEGORY = "category";
    public static final String DOC_TYPE_COLOR = "color";
    public static final String DOC_TYPE_SIZE = "size";

    // ==================== Recommendation Limits ====================
    public static final int MAX_PRODUCTS_FOR_OUTFIT = 20;
    public static final int RECOMMENDATION_LIMIT_OUTFIT = 12;
    public static final int RECOMMENDATION_LIMIT_REGULAR = 8;
    public static final int PRODUCT_COUNT_THRESHOLD = 5;
    public static final int MAX_IMAGES_PER_DETAIL = 3;

    // ==================== String Delimiters ====================
    public static final String DELIMITER_COMMA_SPACE = ", ";
    public static final String DELIMITER_COMMA = ",";
    public static final String EMPTY_STRING = "";

    // ==================== Database Constants ====================
    public static final String TABLE_VECTOR_STORE = "vector_store";
    public static final String SCHEMA_PUBLIC = "public";
    public static final String SQL_TRUNCATE_VECTOR_STORE = "TRUNCATE TABLE vector_store";

    // ==================== Document Building Strings ====================
    public static final String DOC_PRODUCT_INFO = "Product Information:";
    public static final String DOC_PRODUCT_DETAIL_INFO = "Product Detail Information:";
    public static final String DOC_CATEGORY_INFO = "Category Information:";
    public static final String DOC_COLOR_INFO = "Color Information:";
    public static final String DOC_SIZE_INFO = "Size Information:";
    
    public static final String DOC_FIELD_ID = "ID: ";
    public static final String DOC_FIELD_PRODUCT_ID = "Product ID: ";
    public static final String DOC_FIELD_PRODUCT_DETAIL_ID = "ProductDetail ID: ";
    public static final String DOC_FIELD_TITLE = "Title: ";
    public static final String DOC_FIELD_PRODUCT_TITLE = "Product Title: ";
    public static final String DOC_FIELD_DESCRIPTION = "Description: ";
    public static final String DOC_FIELD_CATEGORIES = "Categories: ";
    public static final String DOC_FIELD_COLOR = "Color: ";
    public static final String DOC_FIELD_SIZE = "Size: ";
    public static final String DOC_FIELD_PRICE = "Price: ";
    public static final String DOC_FIELD_STOCK = "Stock: ";
    public static final String DOC_FIELD_IMAGES = "Images: ";
    public static final String DOC_FIELD_NAME = "Name: ";
    public static final String DOC_FIELD_SLUG = "Slug: ";
    public static final String DOC_FIELD_PARENT_CATEGORY = "Parent Category: ";
    public static final String DOC_FIELD_SUBCATEGORIES = "Subcategories: ";
    public static final String DOC_FIELD_HEX_CODE = "Hex Code: ";
    public static final String DOC_FIELD_CODE = "Code: ";
    public static final String DOC_FIELD_LABEL = "Label: ";
    public static final String DOC_FIELD_VARIANTS = "Product Variants:";
    public static final String DOC_FIELD_UNITS = " units";
    public static final String DOC_FIELD_VND = " VND";
    public static final String DOC_FIELD_NA = "N/A";

    // ==================== Response Messages ====================
    public static final String DEFAULT_SHORT_MESSAGE = 
            "Dưới đây là các sản phẩm hiện có trong shop. Nếu bạn cần thêm thông tin chi tiết về sản phẩm nào, hãy cho mình biết nhé!";
    public static final String SHORT_MESSAGE_PREFIX = 
            "Dưới đây là các sản phẩm hiện có trong shop. ";

    // ==================== Controller Response Keys ====================
    public static final String RESPONSE_KEY_ERROR = "error";
    public static final String RESPONSE_KEY_STATUS = "status";
    public static final String RESPONSE_KEY_MESSAGE = "message";
    public static final String RESPONSE_STATUS_SUCCESS = "success";
    public static final String RESPONSE_STATUS_ERROR = "error";
    
    public static final String REINGEST_MESSAGE_SUCCESS = 
            "Product data re-ingestion completed successfully";
    public static final String REINGEST_MESSAGE_FORCE_SUCCESS = 
            "Product data force re-ingestion completed successfully";

    // ==================== Regex Patterns ====================
    public static final class RegexPatterns {
        private RegexPatterns() {}
        
        // Product pattern with ID
        public static final String PRODUCT_PATTERN_WITH_ID = 
                "\\*\\*(.*?)\\*\\*.*?" +
                "- ID:\\s*(\\d+).*?" +
                "- Màu sắc:\\s*(.*?)\\n.*?" +
                "- Kích thước:\\s*(.*?)\\n.*?" +
                "- Giá:\\s*(.*?)\\n.*?" +
                "- Số lượng.*?:\\s*(\\d+).*?" +
                "\\[Xem hình ảnh\\]\\((.*?)\\)";
        
        // Product pattern without ID (fallback)
        public static final String PRODUCT_PATTERN_WITHOUT_ID = 
                "\\*\\*(.*?)\\*\\*.*?" +
                "- Màu sắc:\\s*(.*?)\\n.*?" +
                "- Kích thước:\\s*(.*?)\\n.*?" +
                "- Giá:\\s*(.*?)\\n.*?" +
                "- Số lượng.*?:\\s*(\\d+).*?" +
                "\\[Xem hình ảnh\\]\\((.*?)\\)";
        
        // Short message pattern 1
        public static final String SHORT_MESSAGE_PATTERN_1 = 
                "Dưới đây là.*?shop[.:]?\\s*([^\\n]+(?:\\.|!|\\?))";
        
        // Short message pattern 2
        public static final String SHORT_MESSAGE_PATTERN_2 = 
                "Nếu bạn cần.*?nhé!?";
        
        // Price cleaning pattern
        public static final String PRICE_CLEAN_PATTERN = "[^\\d,]";
    }
}

