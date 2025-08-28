package com.htttql.crmmodule.core.repository;

import com.htttql.crmmodule.core.entity.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IStaffUserRepository extends JpaRepository<StaffUser, Long> {

    Optional<StaffUser> findByPhone(String phone);

    Optional<StaffUser> findByEmail(String email);

    Optional<StaffUser> findByEmailOrPhone(String email, String phone);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    @Query("SELECT s FROM StaffUser s WHERE " +
            "(:search IS NULL OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR s.phone LIKE CONCAT('%', :search, '%') " +
            "OR LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    java.util.List<StaffUser> searchStaffUsers(@Param("search") String search);
}
