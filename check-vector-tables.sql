-- Script để kiểm tra các bảng vector store được tạo bởi Spring AI

-- 1. Kiểm tra pgvector extension
SELECT 
    extname as extension_name,
    extversion as version
FROM pg_extension 
WHERE extname = 'vector';

-- 2. Kiểm tra các bảng vector store (Spring AI tạo)
SELECT 
    schemaname,
    tablename,
    tableowner
FROM pg_tables 
WHERE tablename LIKE '%vector%' 
   OR tablename LIKE '%embedding%'
   OR tablename LIKE '%document%'
ORDER BY tablename;

-- 3. Chi tiết bảng vector_store (bảng chính của Spring AI pgvector)
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'vector_store'
ORDER BY ordinal_position;

-- 4. Đếm số documents trong vector store
SELECT COUNT(*) as total_documents FROM vector_store;

-- 5. Xem một vài documents mẫu (metadata)
SELECT 
    id,
    content,
    metadata,
    embedding IS NOT NULL as has_embedding
FROM vector_store
LIMIT 5;


