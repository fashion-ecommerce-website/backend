package com.spring.fit.backend.product.repository;

import com.spring.fit.backend.product.domain.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    
    @Query("SELECT i FROM Image i WHERE i.url = :url")
    Optional<Image> findByUrl(@Param("url") String url);
}






