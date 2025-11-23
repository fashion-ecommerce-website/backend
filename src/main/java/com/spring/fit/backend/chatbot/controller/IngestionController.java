package com.spring.fit.backend.chatbot.controller;

import com.spring.fit.backend.chatbot.constants.ChatbotConstants;
import com.spring.fit.backend.chatbot.service.ProductIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot/admin")
@CrossOrigin(origins = "*")
public class IngestionController {

    private final ProductIngestionService ingestionService;

    public IngestionController(ProductIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/reingest")
    public ResponseEntity<Map<String, String>> reingest(
            @RequestParam(required = false, defaultValue = "false") boolean force) {
        try {
            if (force) {
                // Force re-ingestion by clearing and re-ingesting
                ingestionService.forceIngest();
            } else {
                // Normal ingestion (will skip if data exists)
                ingestionService.ingestProducts();
            }
            
            Map<String, String> response = new HashMap<>();
            response.put(ChatbotConstants.RESPONSE_KEY_STATUS, ChatbotConstants.RESPONSE_STATUS_SUCCESS);
            response.put(ChatbotConstants.RESPONSE_KEY_MESSAGE, force 
                    ? ChatbotConstants.REINGEST_MESSAGE_FORCE_SUCCESS
                    : ChatbotConstants.REINGEST_MESSAGE_SUCCESS);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put(ChatbotConstants.RESPONSE_KEY_STATUS, ChatbotConstants.RESPONSE_STATUS_ERROR);
            error.put(ChatbotConstants.RESPONSE_KEY_MESSAGE, 
                    ChatbotConstants.ERROR_REINGESTION + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}

