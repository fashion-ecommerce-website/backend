package com.spring.fit.backend.security.controller;


import java.util.List;

import com.spring.fit.backend.security.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.fit.backend.security.domain.dto.AddressRequest;
import com.spring.fit.backend.security.domain.dto.AddressResponse;
import com.spring.fit.backend.security.domain.dto.UpdateUserRequest;
import com.spring.fit.backend.security.domain.dto.UserProfileResponse;
import com.spring.fit.backend.security.service.UserProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/all")
    public ResponseEntity<List<AddressResponse>> getAllAddresses(Authentication authentication) {
        log.info("Getting all addresses for user: {}", authentication.getName());

        List<AddressResponse> addresses = addressService.getAllAddresses(authentication.getName());
        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            Authentication authentication,
            @Valid @RequestBody AddressRequest request) {
        log.info("Creating address for user: {}", authentication.getName());
        return ResponseEntity.ok(addressService.createAddress(authentication.getName(), request));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            Authentication authentication,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        log.info("Updating address {} for user: {}", addressId, authentication.getName());
        return ResponseEntity.ok(addressService.updateAddress(authentication.getName(), addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            Authentication authentication,
            @PathVariable Long addressId) {
        log.info("Deleting address {} for user: {}", addressId, authentication.getName());
        addressService.deleteAddress(authentication.getName(), addressId);
        return ResponseEntity.noContent().build();
    }

}
