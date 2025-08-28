package com.htttql.crmmodule.service.service;

import com.htttql.crmmodule.service.dto.CustomerCaseRequest;
import com.htttql.crmmodule.service.dto.CustomerCaseResponse;
import com.htttql.crmmodule.service.entity.CustomerCase;
import com.htttql.crmmodule.common.enums.CaseStatus;
import com.htttql.crmmodule.service.repository.ICustomerCaseRepository;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of Customer Case Service
 */
@Service
@RequiredArgsConstructor
public class CustomerCaseServiceImpl implements ICustomerCaseService {

    private final ICustomerCaseRepository customerCaseRepository;

    @Override
    public Page<CustomerCaseResponse> getAllCustomerCases(Pageable pageable) {
        Page<CustomerCase> cases = customerCaseRepository.findAll(pageable);
        return cases.map(this::mapToResponse);
    }

    @Override
    public CustomerCaseResponse getCustomerCaseById(Long id) {
        CustomerCase customerCase = customerCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer case not found with id: " + id));
        return mapToResponse(customerCase);
    }

    @Override
    public CustomerCaseResponse createCustomerCase(CustomerCaseRequest request) {
        // Note: This is a simplified implementation
        // In real scenario, you would need to fetch Customer and SpaService entities
        CustomerCase customerCase = CustomerCase.builder()
                .status(CaseStatus.INTAKE) // Default status
                .intakeNote(request.getNotes())
                .build();

        CustomerCase savedCase = customerCaseRepository.save(customerCase);
        return mapToResponse(savedCase);
    }

    @Override
    public CustomerCaseResponse updateCustomerCase(Long id, CustomerCaseRequest request) {
        CustomerCase customerCase = customerCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer case not found with id: " + id));

        customerCase.setIntakeNote(request.getNotes());

        CustomerCase updatedCase = customerCaseRepository.save(customerCase);
        return mapToResponse(updatedCase);
    }

    @Override
    public void deleteCustomerCase(Long id) {
        if (!customerCaseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer case not found with id: " + id);
        }
        customerCaseRepository.deleteById(id);
    }

    @Override
    public CustomerCaseResponse updateCustomerCaseStatus(Long id, String status) {
        CustomerCase customerCase = customerCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer case not found with id: " + id));

        CaseStatus caseStatus = CaseStatus.valueOf(status.toUpperCase());
        customerCase.setStatus(caseStatus);

        CustomerCase updatedCase = customerCaseRepository.save(customerCase);
        return mapToResponse(updatedCase);
    }

    private CustomerCaseResponse mapToResponse(CustomerCase customerCase) {
        return CustomerCaseResponse.builder()
                .caseId(customerCase.getCaseId())
                .customerId(customerCase.getCustomer() != null ? customerCase.getCustomer().getCustomerId() : null)
                .serviceId(customerCase.getPrimaryService() != null ? customerCase.getPrimaryService().getServiceId()
                        : null)
                .status(customerCase.getStatus())
                .startDate(null) // Not available in entity
                .endDate(null) // Not available in entity
                .notes(customerCase.getIntakeNote())
                .createdAt(customerCase.getCreatedAt())
                .updatedAt(customerCase.getUpdatedAt())
                .build();
    }
}
