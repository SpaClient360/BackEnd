package com.htttql.crmmodule.core.service;

import com.htttql.crmmodule.common.exception.BadRequestException;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import com.htttql.crmmodule.core.dto.CreateCustomerRequest;
import com.htttql.crmmodule.core.dto.CustomerDto;
import com.htttql.crmmodule.core.dto.UpdateCustomerRequest;
import com.htttql.crmmodule.core.entity.Customer;
import com.htttql.crmmodule.core.entity.Tier;
import com.htttql.crmmodule.core.repository.CustomerRepository;
import com.htttql.crmmodule.core.repository.TierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final TierRepository tierRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<CustomerDto> getAllCustomers(int page, int size) {
        Page<Customer> customers = customerRepository.findAllWithPagination(PageRequest.of(page, size));
        return customers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> searchCustomers(String search) {
        List<Customer> customers = customerRepository.searchCustomers(search);
        return customers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> getCustomersByTierCode(String tierCode) {
        List<Customer> customers = customerRepository
                .findByTierCode(com.htttql.crmmodule.common.enums.TierCode.valueOf(tierCode));
        return customers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return toDto(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerByPhone(String phone) {
        Customer customer = customerRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "phone", phone));
        return toDto(customer);
    }

    @Transactional
    public CustomerDto createCustomer(CreateCustomerRequest request) {
        // Check if phone already exists
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number already exists");
        }

        // Check if email already exists
        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Get default tier (REGULAR)
        Tier defaultTier = tierRepository.findByCode("REGULAR")
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
        log.info("Created new customer: {}", customer.getCustomerId());

        return toDto(customer);
    }

    @Transactional
    public CustomerDto updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        // Check phone uniqueness if changed
        if (request.getPhone() != null && !request.getPhone().equals(customer.getPhone())) {
            if (customerRepository.existsByPhone(request.getPhone())) {
                throw new BadRequestException("Phone number already exists");
            }
        }

        // Check email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
        }

        // Update fields
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
        log.info("Updated customer: {}", customer.getCustomerId());

        return toDto(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", "id", id);
        }

        customerRepository.deleteById(id);
        log.info("Deleted customer: {}", id);
    }

    private CustomerDto toDto(Customer customer) {
        CustomerDto dto = modelMapper.map(customer, CustomerDto.class);

        // Map tier information
        if (customer.getTier() != null) {
            dto.setTierCode(customer.getTier().getCode().name());
            dto.setTierName(customer.getTier().getCode().getDescription());
        }

        return dto;
    }

    @Override
    public boolean existsByPhone(String phone) {
        return customerRepository.existsByPhone(phone);
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }
}