package com.htttql.crmmodule.service.service;

import com.htttql.crmmodule.service.dto.CustomerCaseRequest;
import com.htttql.crmmodule.service.dto.CustomerCaseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Customer Case operations
 */
public interface ICustomerCaseService {

    /**
     * Get all customer cases with pagination
     */
    Page<CustomerCaseResponse> getAllCustomerCases(Pageable pageable);

    /**
     * Get customer case by ID
     */
    CustomerCaseResponse getCustomerCaseById(Long id);

    /**
     * Create new customer case
     */
    CustomerCaseResponse createCustomerCase(CustomerCaseRequest request);

    /**
     * Update customer case
     */
    CustomerCaseResponse updateCustomerCase(Long id, CustomerCaseRequest request);

    /**
     * Delete customer case
     */
    void deleteCustomerCase(Long id);

    /**
     * Update customer case status
     */
    CustomerCaseResponse updateCustomerCaseStatus(Long id, String status);
}
