package com.spring.fit.backend.chatbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * VectorStoreInitializer - Tự động tạo pgvector extension khi ứng dụng khởi động
 * 
 * Service này kiểm tra và tạo pgvector extension nếu chưa có.
 * Khi sử dụng Docker image pgvector/pgvector, extension thường đã có sẵn,
 * nhưng service này đảm bảo extension được tạo trong database cụ thể.
 */
@Component
@Order(1) // Chạy trước ProductIngestionService
public class VectorStoreInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public VectorStoreInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing pgvector extension...");
        
        try {
            // Kiểm tra xem extension đã tồn tại trong database chưa
            String checkQuery = """
                SELECT EXISTS(
                    SELECT 1 
                    FROM pg_extension 
                    WHERE extname = 'vector'
                )
                """;
            
            Boolean extensionExists = jdbcTemplate.queryForObject(checkQuery, Boolean.class);
            
            if (Boolean.TRUE.equals(extensionExists)) {
                log.info("✓ pgvector extension already exists in database");
                
                // Verify extension is working
                try {
                    String verifyQuery = "SELECT extversion FROM pg_extension WHERE extname = 'vector'";
                    String version = jdbcTemplate.queryForObject(verifyQuery, String.class);
                    log.info("✓ pgvector extension verified. Version: {}", version);
                } catch (Exception e) {
                    log.warn("Could not get pgvector version, but extension exists");
                }
            } else {
                log.info("pgvector extension not found in database. Creating extension...");
                
                try {
                    jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
                    log.info("✓ pgvector extension created successfully");
                } catch (Exception e) {
                    log.error("✗ Failed to create pgvector extension: {}", e.getMessage());
                    log.error("");
                    log.error("Possible solutions:");
                    log.error("1. Make sure you're using pgvector/pgvector Docker image");
                    log.error("2. Grant CREATEEXTENSION privilege to your database user:");
                    log.error("   GRANT CREATE ON DATABASE fit_db TO postgres;");
                    log.error("3. Or create extension manually:");
                    log.error("   CREATE EXTENSION IF NOT EXISTS vector;");
                    throw new RuntimeException(
                        "pgvector extension is required but could not be created. " +
                        "Please ensure you're using pgvector/pgvector Docker image or create the extension manually.", 
                        e
                    );
                }
            }
            
        } catch (Exception e) {
            log.error("Error initializing pgvector extension", e);
            throw e;
        }
    }
}

