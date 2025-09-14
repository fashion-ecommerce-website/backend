package com.spring.fit.backend.user.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.user.domain.dto.request.CreateAddressRequest;
import com.spring.fit.backend.user.domain.dto.request.UpdateAddressRequest;
import com.spring.fit.backend.user.domain.dto.response.AddressResponse;
import com.spring.fit.backend.user.domain.entity.AddressEntity;
import com.spring.fit.backend.user.repository.AddressRepository;
import com.spring.fit.backend.user.service.AddressService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public AddressResponse createAddress(String userEmail, CreateAddressRequest request) {
        log.info("Request: {}", request);

        // Find user
        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        // If this address is set as default, unset other default addresses first
        if (request.isDefault()) {
            log.info("Setting address as default, unsetting other default addresses for user: {}", userEmail);
            unsetOtherDefaultAddresses(user.getId());
        }

        // Create address entity
        AddressEntity address = AddressEntity.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .line(request.getLine())
                .ward(request.getWard())
                .city(request.getCity())
                .countryCode(request.getCountryCode() != null ? request.getCountryCode() : "VN")
                .isDefault(request.isDefault())
                .build();

        log.info("Created address entity with isDefault: {} for user: {}", address.isDefault(), userEmail);
        
        AddressEntity savedAddress = addressRepository.save(address);
        log.info("Address created with ID: {} and isDefault: {} for user: {}", 
                savedAddress.getId(), savedAddress.isDefault(), userEmail);

        return AddressResponse.fromEntity(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(String userEmail) {
        log.info("Getting addresses for user: {}", userEmail);

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        List<AddressEntity> addresses = addressRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<AddressResponse> responses = addresses.stream()
                .map(AddressResponse::fromEntity)
                .collect(Collectors.toList());

        log.info("Found {} addresses for user: {}", responses.size(), userEmail);
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(String userEmail, Long addressId) {
        log.info("Getting address ID: {} for user: {}", addressId, userEmail);

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        AddressEntity address = addressRepository.findByUserIdAndId(user.getId(), addressId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Address not found"));

        return AddressResponse.fromEntity(address);
    }

    @Override
    public AddressResponse updateAddress(String userEmail, Long addressId, UpdateAddressRequest request) {
        log.info("Updating address ID: {} for user: {}", addressId, userEmail);

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        AddressEntity address = addressRepository.findByUserIdAndId(user.getId(), addressId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Address not found"));

        // Update fields if provided
        if (request.getFullName() != null) {
            address.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            address.setPhone(request.getPhone());
        }
        if (request.getLine() != null) {
            address.setLine(request.getLine());
        }
        if (request.getWard() != null) {
            address.setWard(request.getWard());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getCountryCode() != null) {
            address.setCountryCode(request.getCountryCode());
        }
        if (request.getIsDefault() != null) {
            address.setDefault(request.getIsDefault());
            // If setting as default, unset other default addresses
            if (request.getIsDefault()) {
                unsetOtherDefaultAddresses(user.getId(), addressId);
            }
        }

        AddressEntity updatedAddress = addressRepository.save(address);
        log.info("Address ID: {} updated for user: {}", addressId, userEmail);

        return AddressResponse.fromEntity(updatedAddress);
    }

    @Override
    public void deleteAddress(String userEmail, Long addressId) {
        log.info("Deleting address ID: {} for user: {}", addressId, userEmail);

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        AddressEntity address = addressRepository.findByUserIdAndId(user.getId(), addressId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Address not found"));

        addressRepository.delete(address);
        log.info("Address ID: {} deleted for user: {}", addressId, userEmail);
    }

    @Override
    public AddressResponse setDefaultAddress(String userEmail, Long addressId) {
        log.info("Setting default address ID: {} for user: {}", addressId, userEmail);

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        AddressEntity address = addressRepository.findByUserIdAndId(user.getId(), addressId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Address not found"));

        // Unset other default addresses
        unsetOtherDefaultAddresses(user.getId(), addressId);

        // Set this address as default
        address.setDefault(true);
        AddressEntity updatedAddress = addressRepository.save(address);

        log.info("Address ID: {} set as default for user: {}", addressId, userEmail);
        return AddressResponse.fromEntity(updatedAddress);
    }

    private void unsetOtherDefaultAddresses(Long userId) {
        unsetOtherDefaultAddresses(userId, null);
    }

    private void unsetOtherDefaultAddresses(Long userId, Long excludeAddressId) {
        List<AddressEntity> defaultAddresses = addressRepository.findByUserIdAndIsDefaultTrue(userId);
        for (AddressEntity defaultAddress : defaultAddresses) {
            if (excludeAddressId == null || !defaultAddress.getId().equals(excludeAddressId)) {
                defaultAddress.setDefault(false);
                addressRepository.save(defaultAddress);
            }
        }
    }
}
