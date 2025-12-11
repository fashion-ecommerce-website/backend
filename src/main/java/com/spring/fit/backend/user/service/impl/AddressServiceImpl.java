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
        log.info("Inside AddressServiceImpl.createAddress userEmail={}, request={}", userEmail, request);

        // Find user
        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        // If this address is set as default, unset other default addresses first
        if (request.isDefault()) {
            log.info("Inside AddressServiceImpl.createAddress setDefault userEmail={}", userEmail);
            unsetOtherDefaultAddresses(user.getId());
        }

        // Create address entity
        AddressEntity address = AddressEntity.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .line(request.getLine())
                .ward(request.getWard())
                .wardCode(request.getWardCode())
                .city(request.getCity())
                .provinceId(request.getProvinceId())
                .districtName(request.getDistrictName())
                .districtId(request.getDistrictId())
                .countryCode(request.getCountryCode() != null ? request.getCountryCode() : "VN")
                .isDefault(request.isDefault())
                .build();

        log.info("Inside AddressServiceImpl.createAddress created isDefault={}, userEmail={}", address.isDefault(), userEmail);
        
        AddressEntity savedAddress = addressRepository.save(address);
        log.info("Inside AddressServiceImpl.createAddress success addressId={}, isDefault={}, userEmail={}", 
                savedAddress.getId(), savedAddress.isDefault(), userEmail);

        return AddressResponse.fromEntity(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(String userEmail) {
        log.info("Inside AddressServiceImpl.getUserAddresses userEmail={}", userEmail);

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        List<AddressEntity> addresses = addressRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<AddressResponse> responses = addresses.stream()
                .map(AddressResponse::fromEntity)
                .toList();

        log.info("Inside AddressServiceImpl.getUserAddresses success userEmail={}, count={}", userEmail, responses.size());
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(String userEmail, Long addressId) {
        log.info("Inside AddressServiceImpl.getAddressById userEmail={}, addressId={}", userEmail, addressId);

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        AddressEntity address = addressRepository.findByUserIdAndId(user.getId(), addressId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Address not found"));

        return AddressResponse.fromEntity(address);
    }

    @Override
    public AddressResponse updateAddress(String userEmail, Long addressId, UpdateAddressRequest request) {
        log.info("Inside AddressServiceImpl.updateAddress userEmail={}, addressId={}", userEmail, addressId);

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
        if (request.getWardCode() != null) {
            address.setWardCode(request.getWardCode());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getProvinceId() != null) {
            address.setProvinceId(request.getProvinceId());
        }
        if (request.getDistrictName() != null) {
            address.setDistrictName(request.getDistrictName());
        }
        if (request.getDistrictId() != null) {
            address.setDistrictId(request.getDistrictId());
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
        log.info("Inside AddressServiceImpl.updateAddress success userEmail={}, addressId={}", userEmail, addressId);

        return AddressResponse.fromEntity(updatedAddress);
    }

    @Override
    public void deleteAddress(String userEmail, Long addressId) {
        log.info("Inside AddressServiceImpl.deleteAddress userEmail={}, addressId={}", userEmail, addressId);

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        AddressEntity address = addressRepository.findByUserIdAndId(user.getId(), addressId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Address not found"));

        addressRepository.delete(address);
        log.info("Inside AddressServiceImpl.deleteAddress success userEmail={}, addressId={}", userEmail, addressId);
    }

    @Override
    public AddressResponse setDefaultAddress(String userEmail, Long addressId) {
        log.info("Inside AddressServiceImpl.setDefaultAddress userEmail={}, addressId={}", userEmail, addressId);

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        AddressEntity address = addressRepository.findByUserIdAndId(user.getId(), addressId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Address not found"));

        // Unset other default addresses
        unsetOtherDefaultAddresses(user.getId(), addressId);

        // Set this address as default
        address.setDefault(true);
        AddressEntity updatedAddress = addressRepository.save(address);

        log.info("Inside AddressServiceImpl.setDefaultAddress success userEmail={}, addressId={}", userEmail, addressId);
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
