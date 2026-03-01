package com.example.rolebase.repository;

import com.example.rolebase.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            UPDATE RefreshToken rt
            SET rt.revoked = true, rt.revokedAt = :revokedAt
            WHERE rt.user.id = :userId AND rt.revoked = false
            """)
    void revokeAllActiveTokensByUserId(@Param("userId") Long userId, @Param("revokedAt") LocalDateTime revokedAt);
}
