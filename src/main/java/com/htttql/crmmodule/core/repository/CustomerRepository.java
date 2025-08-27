package com.htttql.crmmodule.core.repository;

import com.htttql.crmmodule.common.enums.TierCode;
import com.htttql.crmmodule.core.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Customer entity
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE c.tier.code = :tierCode")
    List<Customer> findByTierCode(@Param("tierCode") TierCode tierCode);

    @Query("SELECT c FROM Customer c WHERE " +
            "(:search IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR c.phone LIKE CONCAT('%', :search, '%') " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Customer> searchCustomers(@Param("search") String search);

    @Query("SELECT c FROM Customer c ORDER BY c.createdAt DESC")
    Page<Customer> findAllWithPagination(Pageable pageable);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.tier.code = :tierCode")
    long countByTierCode(@Param("tierCode") TierCode tierCode);
}
