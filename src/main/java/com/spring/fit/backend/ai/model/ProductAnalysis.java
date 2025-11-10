package com.spring.fit.backend.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAnalysis {
    private List<String> styles;
    private List<String> suitableForBodyTypes;
    private List<String> suitableForWeather;
    private List<String> occasions;
    private String analysis;
}
