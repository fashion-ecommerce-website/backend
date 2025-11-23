package com.spring.fit.backend.chatbot.controller;

import com.spring.fit.backend.chatbot.constants.ChatbotConstants;
import com.spring.fit.backend.chatbot.domain.dto.ChatbotRequest;
import com.spring.fit.backend.chatbot.domain.dto.ChatbotResponse;
import com.spring.fit.backend.chatbot.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatbotRequest request) {
        try {
            ChatbotResponse response = chatbotService.processChat(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put(ChatbotConstants.RESPONSE_KEY_ERROR, e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put(ChatbotConstants.RESPONSE_KEY_ERROR, 
                    ChatbotConstants.ERROR_PROCESSING_CHAT + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

}

