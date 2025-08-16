package com.htttql.crmmodule.auth.repository;

import com.htttql.crmmodule.auth.entity.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for OTP entity
 */
@Repository
public interface OTPRepository extends JpaRepository<OTP, Long> {

    /**
     * Find valid OTP by user ID and code
     */
    @Query("SELECT o FROM OTP o WHERE o.userId = :userId AND o.code = :code AND o.expiredAt > :now")
    Optional<OTP> findValidOTP(@Param("userId") Long userId, 
                              @Param("code") String code, 
                              @Param("now") LocalDateTime now);

    /**
     * Find all OTPs by user ID
     */
    List<OTP> findByUserId(Long userId);

    /**
     * Find latest OTP by user ID
     */
    Optional<OTP> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Delete expired OTPs
     */
    @Modifying
    @Query("DELETE FROM OTP o WHERE o.expiredAt < :now")
    void deleteExpiredOTPs(@Param("now") LocalDateTime now);

    /**
     * Delete all OTPs by user ID
     */
    void deleteByUserId(Long userId);

    /**
     * Count valid OTPs by user ID
     */
    @Query("SELECT COUNT(o) FROM OTP o WHERE o.userId = :userId AND o.expiredAt > :now")
    long countValidOTPsByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}