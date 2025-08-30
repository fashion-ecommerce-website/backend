package com.spring.fit.backend.security.service.impl;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.security.domain.dto.AddressRequest;
import com.spring.fit.backend.security.domain.dto.AddressResponse;
import com.spring.fit.backend.security.domain.entity.Address;
import com.spring.fit.backend.security.domain.entity.User;
import com.spring.fit.backend.security.repository.AddressRepository;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.security.service.AddressService;
import com.spring.fit.backend.security.service.UserProfileService;
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
public class AddressServiceImpl implements AddressService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Override
    public List<AddressResponse> getAllAddresses(String userEmail) {
        log.info("Getting all addresses for user: {}", userEmail);

        User user = userRepository.findActiveUserByEmail(userEmail)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId());

        return addresses.stream()
                .map(this::buildAddressResponse)
                .toList();
    }


    @Override
    @Transactional
    public AddressResponse createAddress(String userEmail, AddressRequest request) {
        log.info("Creating address for user: {}", userEmail);

        User user = userRepository.findActiveUserByEmail(userEmail)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        // Nếu đây là địa chỉ đầu tiên hoặc được đặt làm mặc định
        boolean shouldSetAsDefault = request.isDefault();
        List<Address> existingAddresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId());

        if (existingAddresses.isEmpty()) {
            shouldSetAsDefault = true; // Địa chỉ đầu tiên luôn là mặc định
        }

        // Nếu đặt làm mặc định, cập nhật các địa chỉ khác
        if (shouldSetAsDefault) {
            addressRepository.updateDefaultAddressForUser(user.getId(), false);
        }

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .line(request.getLine())
                .ward(request.getWard())
                .city(request.getCity())
                .province(request.getProvince())
                .countryCode(request.getCountryCode() != null ? request.getCountryCode() : "VN")
                .postalCode(request.getPostalCode())
                .isDefault(shouldSetAsDefault)
                .build();

        address = addressRepository.save(address);
        log.info("Created address with ID: {} for user: {}", address.getId(), userEmail);

        return buildAddressResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(String userEmail, Long addressId, AddressRequest request) {
        log.info("Updating address {} for user: {}", addressId, userEmail);

        User user = userRepository.findActiveUserByEmail(userEmail)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Address not found"));

        // Kiểm tra quyền sở hữu
        if (!address.getUser().getId().equals(user.getId())) {
            throw new ErrorException(HttpStatus.FORBIDDEN, "Access denied to this address");
        }

        // Nếu đặt làm mặc định, cập nhật các địa chỉ khác
        if (request.isDefault() && !address.isDefault()) {
            addressRepository.updateDefaultAddressForUser(user.getId(), false);
        }

        // Cập nhật thông tin địa chỉ
        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setLine(request.getLine());
        address.setWard(request.getWard());
        address.setCity(request.getCity());
        address.setProvince(request.getProvince());
        if (request.getCountryCode() != null) {
            address.setCountryCode(request.getCountryCode());
        }
        address.setPostalCode(request.getPostalCode());
        address.setDefault(request.isDefault());

        address = addressRepository.save(address);
        log.info("Updated address {} for user: {}", addressId, userEmail);

        return buildAddressResponse(address);
    }

    @Override
    @Transactional
    public void deleteAddress(String userEmail, Long addressId) {
        log.info("Deleting address {} for user: {}", addressId, userEmail);

        User user = userRepository.findActiveUserByEmail(userEmail)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Address not found"));

        // Kiểm tra quyền sở hữu
        if (!address.getUser().getId().equals(user.getId())) {
            throw new ErrorException(HttpStatus.FORBIDDEN, "Access denied to this address");
        }

        boolean wasDefault = address.isDefault();
        addressRepository.delete(address);

        // Nếu xóa địa chỉ mặc định, đặt địa chỉ khác làm mặc định
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId());
            if (!remainingAddresses.isEmpty()) {
                Address newDefaultAddress = remainingAddresses.get(0);
                newDefaultAddress.setDefault(true);
                addressRepository.save(newDefaultAddress);
                log.info("Set address {} as new default for user: {}", newDefaultAddress.getId(), userEmail);
            }
        }

        log.info("Deleted address {} for user: {}", addressId, userEmail);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(String userEmail, Long addressId) {
        log.info("Setting default address {} for user: {}", addressId, userEmail);

        User user = userRepository.findActiveUserByEmail(userEmail)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Address not found"));

        // Kiểm tra quyền sở hữu
        if (!address.getUser().getId().equals(user.getId())) {
            throw new ErrorException(HttpStatus.FORBIDDEN, "Access denied to this address");
        }

        // Cập nhật tất cả địa chỉ khác thành không mặc định
        addressRepository.updateDefaultAddressForUser(user.getId(), false);

        // Đặt địa chỉ này làm mặc định
        address.setDefault(true);
        address = addressRepository.save(address);

        log.info("Set address {} as default for user: {}", addressId, userEmail);

        return buildAddressResponse(address);
    }

    /**
     * Xây dựng AddressInfo từ Address entity
     */
    private AddressResponse buildAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .line(address.getLine())
                .ward(address.getWard())
                .city(address.getCity())
                .province(address.getProvince())
                .countryCode(address.getCountryCode())
                .postalCode(address.getPostalCode())
                .isDefault(address.isDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
