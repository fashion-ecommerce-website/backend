package com.spring.fit.backend.ai.dto;

import com.spring.fit.backend.recommendation.domain.enums.BodyType;
import com.spring.fit.backend.recommendation.domain.enums.Occasion;
import com.spring.fit.backend.recommendation.domain.enums.Season;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAnalysisDTO {
    private String productId;
    private String name;
    private String description;
    private List<String> styles;
    private List<String> colors;
    private List<String> patterns;
    private List<String> materials;
    private List<BodyType> suitableForBodyTypes;
    private List<Season> seasons;
    private List<Occasion> occasions;
    private Integer formalityLevel; // 1-5 scale
    private Integer warmthLevel;    // 1-5 scale
    private String dominantColor;
    private String styleDescription;
    private String suitableFor;
    private String fashionAdvice;
    private String additionalNotes;
}
