package com.htttql.crmmodule.service.repository;

import com.htttql.crmmodule.service.entity.CustomerCase;
import com.htttql.crmmodule.service.enums.CaseServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for CustomerCase entity
 */
@Repository
public interface ICustomerCaseRepository extends JpaRepository<CustomerCase, Long> {
    
    /**
     * Find customer cases by customer ID
     */
    List<CustomerCase> findByCustomer_CustomerId(Long customerId);
    
    /**
     * Find customer cases by status
     */
    List<CustomerCase> findByStatus(CaseServiceStatus status);
    
    /**
     * Find customer cases by service ID
     */
    List<CustomerCase> findByPrimaryService_ServiceId(Long serviceId);
}
