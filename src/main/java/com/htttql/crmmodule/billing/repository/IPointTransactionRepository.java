package com.htttql.crmmodule.billing.repository;

import com.htttql.crmmodule.billing.entity.PointTransaction;
import com.htttql.crmmodule.core.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    List<PointTransaction> findByCustomerOrderByCreatedAtDesc(Customer customer);

    @Query("SELECT pt FROM PointTransaction pt WHERE pt.customer.customerId = :customerId ORDER BY pt.createdAt DESC")
    List<PointTransaction> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);

    @Query("SELECT SUM(pt.points) FROM PointTransaction pt WHERE pt.customer.customerId = :customerId AND pt.source = 'EARN'")
    Integer getTotalEarnedPointsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT SUM(pt.points) FROM PointTransaction pt WHERE pt.customer.customerId = :customerId AND pt.source = 'REDEEM'")
    Integer getTotalRedeemedPointsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT SUM(pt.points) FROM PointTransaction pt WHERE pt.customer.customerId = :customerId")
    Integer getNetPointsByCustomerId(@Param("customerId") Long customerId);
}
