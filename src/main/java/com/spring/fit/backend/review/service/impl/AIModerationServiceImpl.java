package com.spring.fit.backend.review.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.fit.backend.review.domain.dto.response.ReviewModerationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; // Đảm bảo đúng import này
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AIModerationServiceImpl {


        @Value("${perspective.api.key}")
        private String apiKey;

        @Value("${perspective.api.url}")
        private String apiUrl;

        private final WebClient webClient = WebClient.create();
        private final ObjectMapper mapper = new ObjectMapper();

        // Regex cải tiến để chặn cả tiếng Anh thô tục
        private static final Pattern BAD_WORDS = Pattern.compile(
                "(?i)\\b(vcl|dm|đm|dmm|cc|clm|vcc|ngu|óc chó|lol|cứt|lồn|chim)\\b"
        );

        public ReviewModerationResponse verifyContent(String content) {
            // 1. Chặn nhanh bằng Regex (Lớp bảo vệ 1)
            if (BAD_WORDS.matcher(content).find()) {
                log.warn("Chặn nhanh bằng Regex: [{}]", content);
                return new ReviewModerationResponse(false);
            }

            try {
                // Perspective hỗ trợ tốt nhất khi dùng TOXICITY (Model chuẩn)
                Map<String, Object> requestBody = Map.of(
                        "comment", Map.of("text", content),
                        "requestedAttributes", Map.of("TOXICITY", Map.of())
                );

                String response = webClient.post()
                        .uri(apiUrl + "?key=" + apiKey)
                        .header("Content-Type", "application/json")
                        .bodyValue(requestBody)
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError(), clientResponse ->
                                clientResponse.bodyToMono(JsonNode.class)
                                        .flatMap(errorNode -> {
                                            String msg = errorNode.path("error").path("message").asText();
                                            log.error("Perspective API Error: {}", msg);
                                            return Mono.error(new Exception("API_ERROR"));
                                        })
                        )
                        .bodyToMono(String.class)
                        .block();

                return parsePerspectiveResponse(response);

            } catch (Exception e) {
                // Không log stack trace nữa, chỉ hiện cảnh báo ngắn
                log.warn("⚠️ AI Moderation gặp sự cố hoặc ngôn ngữ chưa hỗ trợ -> Tạm thời cho qua.");
                return new ReviewModerationResponse(true);
            }
        }

        private ReviewModerationResponse parsePerspectiveResponse(String response) {
            try {
                JsonNode root = mapper.readTree(response);
                // Quan trọng: Phải lấy đúng thuộc tính đã request ở trên (TOXICITY)
                JsonNode toxicityNode = root.path("attributeScores").path("TOXICITY");

                if (toxicityNode.isMissingNode()) {
                    log.warn("Không tìm thấy score TOXICITY trong response");
                    return new ReviewModerationResponse(true);
                }

                double score = toxicityNode.path("summaryScore").path("value").asDouble();
                log.info("📊 AI Toxicity Score: {}", score);

                return new ReviewModerationResponse(score < 0.7);            } catch (Exception e) {
                return new ReviewModerationResponse(true);
            }
        }
    }
