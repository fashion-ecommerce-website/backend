package com.spring.fit.backend.recommendation.domain.entity;

import com.spring.fit.backend.product.domain.entity.Product;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    // Basic analysis
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> styles;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> colors;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> patterns;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> materials;

    // Compatibility
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> suitableForBodyTypes;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> suitableForWeather;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> occasions;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> seasons;

    // Rating
    private Integer formalityLevel; // 1-5: casual -> formal
    private Integer warmthLevel;    // 1-5: light -> warm
    
    // Detailed analysis
    @Column(columnDefinition = "text")
    private String aiAnalysis;
    
    @Column(name = "last_analyzed_at")
    private LocalDateTime lastAnalyzedAt;
    
    @Column(name = "is_analyzed")
    private Boolean isAnalyzed = false;
    
    @Version
    private Long version;
    
    // Initialize null collections to prevent NPE
    @PostLoad
    @PrePersist
    @PreUpdate
    public void initializeNullCollections() {
        if (this.styles == null) this.styles = new java.util.ArrayList<>();
        if (this.colors == null) this.colors = new java.util.ArrayList<>();
        if (this.patterns == null) this.patterns = new java.util.ArrayList<>();
        if (this.materials == null) this.materials = new java.util.ArrayList<>();
        if (this.suitableForBodyTypes == null) this.suitableForBodyTypes = new java.util.ArrayList<>();
        if (this.suitableForWeather == null) this.suitableForWeather = new java.util.ArrayList<>();
        if (this.occasions == null) this.occasions = new java.util.ArrayList<>();
        if (this.seasons == null) this.seasons = new java.util.ArrayList<>();
        if (this.formalityLevel == null) this.formalityLevel = 3; // Default middle value
        if (this.warmthLevel == null) this.warmthLevel = 3; // Default middle value
        if (this.isAnalyzed == null) this.isAnalyzed = false;
    }
    
    // Convenience method to check if metadata is analyzed
    public boolean isAnalyzed() {
        return Boolean.TRUE.equals(isAnalyzed);
    }
    
    // Helper method to set analyzed status
    public void setIsAnalyzed(boolean isAnalyzed) {
        this.isAnalyzed = isAnalyzed;
    }
}
