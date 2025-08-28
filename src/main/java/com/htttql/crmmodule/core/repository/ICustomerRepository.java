package com.htttql.crmmodule.core.repository;

import com.htttql.crmmodule.core.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE " +
            "(:search IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR c.phone LIKE CONCAT('%', :search, '%') " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    java.util.List<Customer> searchCustomers(@Param("search") String search);
}
