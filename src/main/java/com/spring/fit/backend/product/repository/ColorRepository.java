package com.spring.fit.backend.product.repository;

import com.spring.fit.backend.product.domain.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Short> {
    Optional<Color> findByName(String name);
}






