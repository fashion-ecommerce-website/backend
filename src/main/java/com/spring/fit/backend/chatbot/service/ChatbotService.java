package com.spring.fit.backend.chatbot.service;

import com.spring.fit.backend.chatbot.domain.dto.ChatbotRequest;
import com.spring.fit.backend.chatbot.domain.dto.ChatbotResponse;

public interface ChatbotService {
    ChatbotResponse processChat(ChatbotRequest request);
}
