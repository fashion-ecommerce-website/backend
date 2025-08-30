package com.spring.fit.backend.security.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spring.fit.backend.security.domain.entity.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
    
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.id != :addressId")
    void unsetOtherDefaultAddresses(@Param("userId") Long userId, @Param("addressId") Long addressId);
    
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = :isDefault WHERE a.user.id = :userId")
    void updateDefaultAddressForUser(@Param("userId") Long userId, @Param("isDefault") boolean isDefault);
    
    boolean existsByUserIdAndId(Long userId, Long id);
    
    long countByUserId(Long userId);
} 