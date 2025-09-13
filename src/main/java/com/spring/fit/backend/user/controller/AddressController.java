package com.spring.fit.backend.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.spring.fit.backend.user.domain.dto.AddressResponse;
import com.spring.fit.backend.user.domain.dto.CreateAddressRequest;
import com.spring.fit.backend.user.domain.dto.UpdateAddressRequest;
import com.spring.fit.backend.user.service.AddressService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(@Valid @RequestBody CreateAddressRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AddressResponse address = addressService.createAddress(email, request);
        return ResponseEntity.ok(address);
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getUserAddresses() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<AddressResponse> addresses = addressService.getUserAddresses(email);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddressById(
            @PathVariable @Positive(message = "Address ID must be positive") Long addressId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AddressResponse address = addressService.getAddressById(email, addressId);
        return ResponseEntity.ok(address);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable @Positive(message = "Address ID must be positive") Long addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AddressResponse address = addressService.updateAddress(email, addressId, request);
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable @Positive(message = "Address ID must be positive") Long addressId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        addressService.deleteAddress(email, addressId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @PathVariable @Positive(message = "Address ID must be positive") Long addressId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AddressResponse address = addressService.setDefaultAddress(email, addressId);
        return ResponseEntity.ok(address);
    }
}
