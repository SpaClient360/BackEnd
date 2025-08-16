package com.htttql.crmmodule.user.repository;

import com.htttql.crmmodule.user.entity.User;
import com.htttql.crmmodule.user.enums.Gender;
import com.htttql.crmmodule.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity (includes both login and staff profile
 * queries)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by phone
     */
    Optional<User> findByPhone(String phone);

    /**
     * Find user by email or phone
     */
    @Query("SELECT u FROM User u WHERE u.email = :emailOrPhone OR u.phone = :emailOrPhone")
    Optional<User> findByEmailOrPhone(@Param("emailOrPhone") String emailOrPhone);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone exists
     */
    boolean existsByPhone(String phone);

    /**
     * Find users by status
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Find users by role id
     */
    List<User> findByRoleId(Long roleId);

    /**
     * Find users by status with pagination
     */
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    /**
     * Find users by role id with pagination
     */
    Page<User> findByRoleId(Long roleId, Pageable pageable);

    /**
     * Search users by email or phone
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:keyword% OR u.phone LIKE %:keyword%")
    Page<User> searchByEmailOrPhone(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find users by full name containing (case insensitive)
     */
    Page<User> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

    /**
     * Find users by position containing (case insensitive)
     */
    Page<User> findByPositionContainingIgnoreCase(String position, Pageable pageable);

    /**
     * Find users by gender
     */
    Page<User> findByGender(Gender gender, Pageable pageable);

    /**
     * Find users by birth date range
     */
    List<User> findByDateOfBirthBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Count users by status
     */
    long countByStatus(UserStatus status);

    /**
     * Count users by role id
     */
    long countByRoleId(Long roleId);

    /**
     * Count users by position
     */
    long countByPosition(String position);

    /**
     * Count users by gender
     */
    long countByGender(Gender gender);
}