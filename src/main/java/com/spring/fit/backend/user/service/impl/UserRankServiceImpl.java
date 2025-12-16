package com.spring.fit.backend.user.service.impl;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.user.domain.dto.response.UserRankResponse;
import com.spring.fit.backend.user.domain.entity.UserRank;
import com.spring.fit.backend.user.repository.UserRankRepository;
import com.spring.fit.backend.user.service.UserRankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserRankServiceImpl implements UserRankService {
    
    private final UserRankRepository userRankRepository;
    
    @Override
    public List<UserRankResponse> getAllUserRanks() {
        log.info("Inside UserRankServiceImpl.getAllUserRanks");
        
        List<UserRank> userRanks = userRankRepository.findAll();
        List<UserRankResponse> responses = userRanks.stream()
                .map(UserRankResponse::fromEntity)
                .toList();
        
        log.info("Inside UserRankServiceImpl.getAllUserRanks success count={}", responses.size());
        return responses;
    }
}

