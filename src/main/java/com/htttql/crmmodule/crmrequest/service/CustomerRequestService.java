package com.htttql.crmmodule.crmrequest.service;

import com.htttql.crmmodule.crmrequest.domain.CustomerRequest;
import com.htttql.crmmodule.crmrequest.dto.CustomerRequestCreateRequest;
import com.htttql.crmmodule.crmrequest.dto.CustomerRequestResponse;
import com.htttql.crmmodule.crmrequest.mapper.CustomerRequestMapper;
import com.htttql.crmmodule.crmrequest.domain.CustomerRequestStatus;
import com.htttql.crmmodule.crmrequest.repository.CustomerRequestRepository;

import java.time.Duration;
import java.time.Instant;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerRequestService {

    private final CustomerRequestRepository repository;
    private final RateLimiter ipRateLimiter;

    public CustomerRequestService(CustomerRequestRepository repository) {
        this.repository = repository;
        // For example: max 5 requests per 1 minute per IP
        this.ipRateLimiter = new RateLimiter(5, Duration.ofMinutes(1));
    }

    @Transactional
    public CustomerRequestResponse create(String ipAddress, CustomerRequestCreateRequest request) {
        // Rate limit by IP (basic in-memory)
        String key = ipAddress == null ? "unknown" : ipAddress;
        if (!ipRateLimiter.allow(key)) {
            throw new TooManyRequestsException("Rate limit exceeded. Please retry later.");
        }

        // Spam prevention: block duplicate submissions within 2 minutes by phone/ip
        Instant within = Instant.now().minus(Duration.ofMinutes(2));
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            if (repository.existsByPhoneNumberAndCreatedAtAfter(request.getPhoneNumber(), within)) {
                throw new DuplicateRequestException("A similar request was just submitted.");
            }
        }
        if (ipAddress != null && repository.existsByIpAddressAndCreatedAtAfter(ipAddress, within)) {
            throw new DuplicateRequestException("A request from this IP was just submitted.");
        }

        // Persist entity
        CustomerRequest entity = CustomerRequestMapper.toEntity(request);
        entity.setIpAddress(ipAddress);
        CustomerRequest saved = repository.save(entity);

        // Return response
        CustomerRequestResponse response = CustomerRequestMapper.toResponse(saved);
        return response;
    }

    @Transactional(readOnly = true)
    public CustomerRequestResponse getOne(UUID id) {
        CustomerRequest entity = repository
                .findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer request not found"));
        return CustomerRequestMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<CustomerRequestResponse> getPage(Pageable pageable) {
        return repository.findAll(pageable).map(CustomerRequestMapper::toResponse);
    }

    @Transactional
    public CustomerRequestResponse updateStatus(UUID id, CustomerRequestStatus newStatus) {
        CustomerRequest entity = repository
                .findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer request not found"));

        entity.setStatus(newStatus);
        CustomerRequest updated = repository.save(entity);

        // Return updated response
        CustomerRequestResponse response = CustomerRequestMapper.toResponse(updated);

        return response;
    }
}
