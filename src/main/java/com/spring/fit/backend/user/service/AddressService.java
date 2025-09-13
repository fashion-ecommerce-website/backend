package com.spring.fit.backend.user.service;

import java.util.List;

import com.spring.fit.backend.user.domain.dto.AddressResponse;
import com.spring.fit.backend.user.domain.dto.CreateAddressRequest;
import com.spring.fit.backend.user.domain.dto.UpdateAddressRequest;

public interface AddressService {

    AddressResponse createAddress(String userEmail, CreateAddressRequest request);

    List<AddressResponse> getUserAddresses(String userEmail);

    AddressResponse getAddressById(String userEmail, Long addressId);

    AddressResponse updateAddress(String userEmail, Long addressId, UpdateAddressRequest request);

    void deleteAddress(String userEmail, Long addressId);

    AddressResponse setDefaultAddress(String userEmail, Long addressId);
}
