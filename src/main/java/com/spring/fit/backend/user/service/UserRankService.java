package com.spring.fit.backend.user.service;

import com.spring.fit.backend.user.domain.dto.response.UserRankResponse;

import java.util.List;

public interface UserRankService {
    
    List<UserRankResponse> getAllUserRanks();
}

