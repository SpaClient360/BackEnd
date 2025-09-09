package com.htttql.crmmodule.core.service;

import com.htttql.crmmodule.common.enums.TierCode;
import com.htttql.crmmodule.common.exception.BadRequestException;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import com.htttql.crmmodule.core.dto.CustomerRequest;
import com.htttql.crmmodule.core.dto.CustomerResponse;
import com.htttql.crmmodule.core.entity.Customer;
import com.htttql.crmmodule.core.entity.Tier;
import com.htttql.crmmodule.core.repository.ICustomerRepository;
import com.htttql.crmmodule.core.repository.ITierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service("customerService")
@RequiredArgsConstructor
public class CustomerServiceImpl implements ICustomerService {

    private final ICustomerRepository customerRepository;
    private final ITierRepository tierRepository;
    private final ICustomerTierService customerTierService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number already exists");
        }
        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        Tier defaultTier = tierRepository.findByCode(TierCode.REGULAR)
                .orElseThrow(() -> new BadRequestException("Default tier not found"));

        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .dob(request.getDob())
                .gender(request.getGender())
                .address(request.getAddress())
                .notes(request.getNotes())
                .isVip(request.getIsVip())
                .tier(defaultTier)
                .build();

        customer = customerRepository.save(customer);
        return toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        if (request.getPhone() != null && !request.getPhone().equals(customer.getPhone())) {
            if (customerRepository.existsByPhone(request.getPhone())) {
                throw new BadRequestException("Phone number already exists");
            }
        }
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
        }

        if (request.getFullName() != null)
            customer.setFullName(request.getFullName());
        if (request.getPhone() != null)
            customer.setPhone(request.getPhone());
        if (request.getEmail() != null)
            customer.setEmail(request.getEmail());
        if (request.getDob() != null)
            customer.setDob(request.getDob());
        if (request.getGender() != null)
            customer.setGender(request.getGender());
        if (request.getAddress() != null)
            customer.setAddress(request.getAddress());
        if (request.getNotes() != null)
            customer.setNotes(request.getNotes());
        if (request.getIsVip() != null)
            customer.setIsVip(request.getIsVip());

        customer = customerRepository.save(customer);
        return toResponse(customer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", "id", id);
        }
        customerRepository.deleteById(id);
    }

    /**
     * Manually refresh customer tier
     */
    @Override
    @Transactional
    public CustomerResponse refreshCustomerTier(Long customerId) {
        return customerTierService.refreshCustomerTier(customerId);
    }

    /**
     * Batch refresh all customers' tiers
     */
    @Override
    @Transactional
    public void refreshAllCustomerTiers() {
        customerTierService.refreshAllCustomerTiers();
    }

    private CustomerResponse toResponse(Customer customer) {
        CustomerResponse response = modelMapper.map(customer, CustomerResponse.class);
        if (customer.getTier() != null) {
            response.setTierCode(customer.getTier().getCode().name());
            response.setTierName(customer.getTier().getCode().getDescription());
        }
        return response;
    }
}
