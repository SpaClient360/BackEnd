package com.htttql.crmmodule.core.service;

import com.htttql.crmmodule.core.dto.CustomerDto;
import com.htttql.crmmodule.core.dto.CreateCustomerRequest;
import com.htttql.crmmodule.core.dto.UpdateCustomerRequest;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Interface for Customer service operations
 */
public interface ICustomerService {

    /**
     * Get all customers with pagination
     */
    List<CustomerDto> getAllCustomers(int page, int size);

    /**
     * Get customer by ID
     */
    CustomerDto getCustomerById(Long id);

    /**
     * Get customer by phone number
     */
    CustomerDto getCustomerByPhone(String phone);

    /**
     * Create new customer
     */
    CustomerDto createCustomer(CreateCustomerRequest request);

    /**
     * Update customer
     */
    CustomerDto updateCustomer(Long id, UpdateCustomerRequest request);

    /**
     * Delete customer
     */
    void deleteCustomer(Long id);

    /**
     * Check if customer exists by phone
     */
    boolean existsByPhone(String phone);

    /**
     * Check if customer exists by email
     */
    boolean existsByEmail(String email);
}
