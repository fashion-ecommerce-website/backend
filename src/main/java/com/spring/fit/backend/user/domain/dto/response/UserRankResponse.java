package com.spring.fit.backend.user.domain.dto.response;

import com.spring.fit.backend.user.domain.entity.UserRank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRankResponse {
    
    private Short id;
    private String code;
    private String name;
    private LocalDateTime createdAt;
    
    public static UserRankResponse fromEntity(UserRank userRank) {
        return UserRankResponse.builder()
                .id(userRank.getId())
                .code(userRank.getCode())
                .name(userRank.getName())
                .createdAt(userRank.getCreatedAt())
                .build();
    }
}

