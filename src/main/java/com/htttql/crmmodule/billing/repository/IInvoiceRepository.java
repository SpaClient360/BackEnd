package com.htttql.crmmodule.billing.repository;

import com.htttql.crmmodule.billing.entity.Invoice;
import com.htttql.crmmodule.common.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IInvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByCustomer_CustomerId(Long customerId);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT i FROM Invoice i WHERE " +
            "(:customerId IS NULL OR i.customer.customerId = :customerId) " +
            "AND (:status IS NULL OR i.status = :status) " +
            "AND (:startDate IS NULL OR i.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR i.createdAt <= :endDate)")
    List<Invoice> findInvoicesByFilters(
            @Param("customerId") Long customerId,
            @Param("status") InvoiceStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
