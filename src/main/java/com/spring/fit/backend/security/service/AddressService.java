package com.spring.fit.backend.security.service;

import com.spring.fit.backend.security.domain.dto.AddressRequest;
import com.spring.fit.backend.security.domain.dto.AddressResponse;

import java.util.List;

public interface AddressService {
    /**
     * Tạo địa chỉ mới cho user
     */
    AddressResponse createAddress(String userEmail, AddressRequest request);

    /**
     * Cập nhật địa chỉ của user
     */
    AddressResponse updateAddress(String userEmail, Long addressId, AddressRequest request);

    /**
     * Xóa địa chỉ của user
     */
    void deleteAddress(String userEmail, Long addressId);

    /**
     * Đặt địa chỉ mặc định cho user
     */
    AddressResponse setDefaultAddress(String userEmail, Long addressId);

    List<AddressResponse> getAllAddresses(String userEmail);


}
