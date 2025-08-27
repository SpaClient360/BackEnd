package com.htttql.crmmodule.core.repository;

import com.htttql.crmmodule.common.enums.StaffStatus;
import com.htttql.crmmodule.core.entity.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for StaffUser entity
 */
@Repository
public interface StaffUserRepository extends JpaRepository<StaffUser, Long> {

    Optional<StaffUser> findByEmail(String email);

    Optional<StaffUser> findByPhone(String phone);

    Optional<StaffUser> findByEmailOrPhone(String email, String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("SELECT s FROM StaffUser s WHERE s.role.code = :roleCode AND s.status = :status")
    java.util.List<StaffUser> findByRoleAndStatus(@Param("roleCode") String roleCode,
            @Param("status") StaffStatus status);

    @Modifying
    @Query("UPDATE StaffUser s SET s.lastLoginAt = :lastLoginAt WHERE s.staffId = :staffId")
    void updateLastLogin(@Param("staffId") Long staffId, @Param("lastLoginAt") LocalDateTime lastLoginAt);
}
