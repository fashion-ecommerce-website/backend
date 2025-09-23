package com.spring.fit.backend.product.repository;

import com.spring.fit.backend.product.domain.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<Size, Short> {
    Optional<Size> findByCode(String code);
}






