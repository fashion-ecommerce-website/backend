package com.spring.fit.backend.common.controller;

import com.spring.fit.backend.common.dto.EnumResponseDto;
import com.spring.fit.backend.common.service.EnumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common")
public class EnumController {

    @Autowired
    private EnumService enumService;

    @GetMapping("/enums")
    public ResponseEntity<EnumResponseDto> getAllEnums() {
        EnumResponseDto enums = enumService.getAllEnums();
        return ResponseEntity.ok(enums);
    }
}
