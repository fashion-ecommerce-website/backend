package com.spring.fit.backend.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to understand query intent and generate better search queries using GPT
 */
@Service
public class QueryIntentService {

    private static final Logger log = LoggerFactory.getLogger(QueryIntentService.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public QueryIntentService(ChatClient.Builder builder, ObjectMapper objectMapper) {
        this.chatClient = builder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Analyze query intent and determine if it's an outfit query
     */
    public boolean isOutfitQuery(String query) {
        try {
            String prompt = String.format("""
                Analyze the following user query and determine if it's asking for an outfit recommendation or styling advice.
                Query: "%s"
                
                Respond with ONLY "true" or "false" (no other text).
                Return "true" if the query is about:
                - Outfit recommendations (outfit, phối, styling, bộ, combo, set)
                - Complete looks or ensembles
                - Coordinating multiple clothing items
                
                Return "false" for:
                - Single product searches
                - Specific product questions
                - Price or availability queries
                """, query);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            if (response == null) {
                log.warn("GPT returned null response for outfit query detection");
                return false;
            }

            return "true".equalsIgnoreCase(response.trim());
        } catch (Exception e) {
            log.warn("Error determining outfit query, defaulting to false: {}", e.getMessage());
            // Fallback to simple keyword check
            String queryLower = query.toLowerCase();
            String[] outfitKeywords = {"outfit", "phối", "styling", "bộ", "combo", "set"};
            for (String keyword : outfitKeywords) {
                if (queryLower.contains(keyword)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Generate diverse search queries for outfit recommendations based on context
     */
    public List<String> generateOutfitSearchQueries(String originalQuery) {
        try {
            String prompt = String.format("""
                Based on the user's outfit request, generate 4-6 specific search queries to find diverse product categories.
                Original query: "%s"
                
                IMPORTANT: Consider the SEASON mentioned in the query:
                - For SUMMER (mùa hè, mùa nóng): Include keywords like "ngắn tay", "áo thun", "quần short", "mỏng", "nhẹ", không nên là sơ mi
                - For WINTER (mùa đông, mùa lạnh): Include keywords like "dài tay", "sweatshirt", "hoodie", "áo len", "dày", "ấm", "quần jogger", sơ mi vẫn có thể
                
                Generate queries that will help find:
                1. Tops/Áo - Match the season (short sleeve for summer, long sleeve/hoodie for winter)
                2. Bottoms/Quần - Match the season (shorts for summer, long pants for winter)
                3. Bags/Túi (tote, đeo vai, đeo chéo, etc.)
                4. Shoes/Giày (optional, if mentioned)
                5. Accessories/Phụ kiện (mũ, nón, etc. - optional)
                
                Each query should be specific to one category type AND include season-appropriate keywords.
                Example for summer: "áo thun ngắn tay mùa hè", "quần short mùa hè"
                Example for winter: "áo sweatshirt dài tay mùa đông", "quần jogger dài mùa đông"
                
                Return ONLY a JSON array of strings, no other text:
                ["query1", "query2", "query3", ...]
                """, originalQuery);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            if (response == null) {
                log.warn("GPT returned null response for outfit search queries");
                return generateFallbackQueries(originalQuery);
            }
            
            response = response.trim();

            // Extract JSON array from response (handle markdown code blocks if present)
            if (response.startsWith("```")) {
                int start = response.indexOf('[');
                int end = response.lastIndexOf(']') + 1;
                if (start >= 0 && end > start) {
                    response = response.substring(start, end);
                }
            }

            @SuppressWarnings("unchecked")
            List<String> queries = objectMapper.readValue(response, List.class);
            
            // Add original query as first query
            List<String> allQueries = new ArrayList<>();
            allQueries.add(originalQuery);
            allQueries.addAll(queries);
            
            log.debug("Generated {} search queries for outfit: {}", allQueries.size(), allQueries);
            return allQueries;
            
        } catch (Exception e) {
            log.warn("Error generating outfit search queries, using fallback: {}", e.getMessage());
            // Fallback to simple category-based queries
            return generateFallbackQueries(originalQuery);
        }
    }

    /**
     * Fallback: Generate simple category-based queries
     */
    private List<String> generateFallbackQueries(String originalQuery) {
        List<String> queries = new ArrayList<>();
        queries.add(originalQuery);
        queries.add(originalQuery + " áo sơ mi polo hoodie");
        queries.add(originalQuery + " quần jogger shorts");
        queries.add(originalQuery + " túi tote đeo vai đeo chéo");
        queries.add(originalQuery + " giày dép sneakers");
        queries.add(originalQuery + " mũ nón phụ kiện");
        return queries;
    }

    /**
     * Extract target categories from query context (for diversity selection)
     */
    public List<String> extractTargetCategories(String query) {
        try {
            String prompt = String.format("""
                Analyze the user query and extract the main product categories they're interested in.
                Query: "%s"
                
                Return a JSON array of category names in Vietnamese (e.g., ["áo", "quần", "túi ví", "giày dép", "mũ nón"]).
                If the query is about outfits, return all relevant categories.
                If the query is about a specific product, return that category only.
                
                Return ONLY the JSON array, no other text.
                """, query);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            if (response == null) {
                log.warn("GPT returned null response for category extraction");
                return List.of("áo", "quần", "túi ví", "giày dép", "mũ nón");
            }
            
            response = response.trim();

            // Extract JSON array
            if (response.startsWith("```")) {
                int start = response.indexOf('[');
                int end = response.lastIndexOf(']') + 1;
                if (start >= 0 && end > start) {
                    response = response.substring(start, end);
                }
            }

            @SuppressWarnings("unchecked")
            List<String> categories = objectMapper.readValue(response, List.class);
            
            log.debug("Extracted target categories: {}", categories);
            return categories;
            
        } catch (Exception e) {
            log.warn("Error extracting categories, using defaults: {}", e.getMessage());
            // Default categories for outfit queries
            return List.of("áo", "quần", "túi ví", "giày dép", "mũ nón");
        }
    }
}

