package com.spring.fit.backend.user.repository;

import com.spring.fit.backend.user.domain.entity.UserRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRankRepository extends JpaRepository<UserRank, Short> {

    Optional<UserRank> findByCode(String code);

    boolean existsByCode(String code);
}


