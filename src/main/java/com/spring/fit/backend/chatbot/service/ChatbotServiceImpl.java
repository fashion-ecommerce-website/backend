package com.spring.fit.backend.chatbot.service;

import com.spring.fit.backend.chatbot.constants.ChatbotConstants;
import com.spring.fit.backend.chatbot.constants.ChatbotPrompts;
import com.spring.fit.backend.chatbot.domain.dto.ChatbotRequest;
import com.spring.fit.backend.chatbot.domain.dto.ChatbotResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotServiceImpl.class);

    private final ChatClient chatClient;
    private final ProductRecommendationService productRecommendationService;
    private final ResponseParser responseParser;

    public ChatbotServiceImpl(
            ChatClient.Builder builder,
            VectorStore vectorStore,
            ProductRecommendationService productRecommendationService,
            ResponseParser responseParser) {
        this.chatClient = builder
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .defaultSystem(ChatbotPrompts.SYSTEM_PROMPT)
                .build();
        this.productRecommendationService = productRecommendationService;
        this.responseParser = responseParser;
    }

    @Override
    public ChatbotResponse processChat(ChatbotRequest request) {
        String message = request.getMessage();
        
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException(ChatbotConstants.ERROR_MESSAGE_EMPTY);
        }

        try {
            // Get response from GPT
            String gptResponse = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();

            // Extract product IDs from vector store search
            Set<Long> productIds = productRecommendationService.extractProductIdsFromQuery(message);
            
            // Build response
            ChatbotResponse chatbotResponse = new ChatbotResponse();
            
            if (!productIds.isEmpty()) {
                // Get product recommendations from database
                List<ChatbotResponse.ProductRecommendation> dbRecommendations = 
                        productRecommendationService.getProductRecommendations(productIds);
                
                // Parse GPT response to extract product info and find exact ProductDetails
                List<ChatbotResponse.ProductRecommendation> recommendations = 
                        responseParser.parseProductRecommendations(gptResponse, productIds, dbRecommendations);
                
                // Extract short message from GPT response
                String shortMessage = responseParser.extractShortMessage(gptResponse);
                
                chatbotResponse.setType(ChatbotConstants.RESPONSE_TYPE_PRODUCT);
                chatbotResponse.setMessage(shortMessage);
                chatbotResponse.setRecommendations(recommendations);
            } else {
                // No products found, just return message
                chatbotResponse.setType(ChatbotConstants.RESPONSE_TYPE_MESSAGE);
                chatbotResponse.setMessage(gptResponse);
                chatbotResponse.setRecommendations(null);
            }

            return chatbotResponse;
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing chat: {}", e.getMessage(), e);
            throw new RuntimeException(ChatbotConstants.ERROR_PROCESSING_CHAT + e.getMessage(), e);
        }
    }
}

