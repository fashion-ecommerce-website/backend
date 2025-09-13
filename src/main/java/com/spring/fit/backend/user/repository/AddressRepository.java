package com.spring.fit.backend.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spring.fit.backend.user.domain.entity.AddressEntity;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {

    List<AddressEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AddressEntity> findByUserIdAndIsDefaultTrue(Long userId);

    @Query("SELECT a FROM AddressEntity a WHERE a.user.id = :userId AND a.id = :addressId")
    Optional<AddressEntity> findByUserIdAndId(@Param("userId") Long userId, @Param("addressId") Long addressId);

    @Query("SELECT a FROM AddressEntity a WHERE a.user.id = :userId AND a.isDefault = true")
    Optional<AddressEntity> findDefaultByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(a) FROM AddressEntity a WHERE a.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
