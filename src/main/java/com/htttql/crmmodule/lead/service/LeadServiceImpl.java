package com.htttql.crmmodule.lead.service;

import com.htttql.crmmodule.common.enums.LeadStatus;
import com.htttql.crmmodule.common.exception.BadRequestException;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import com.htttql.crmmodule.core.entity.Customer;
import com.htttql.crmmodule.core.repository.ICustomerRepository;
import com.htttql.crmmodule.lead.dto.LeadRequest;
import com.htttql.crmmodule.lead.dto.LeadResponse;
import com.htttql.crmmodule.lead.dto.LeadStatusRequest;
import com.htttql.crmmodule.lead.entity.Lead;
import com.htttql.crmmodule.lead.repository.ILeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@Slf4j
@Service("leadService")
@RequiredArgsConstructor
public class LeadServiceImpl implements ILeadService {

    private final ILeadRepository leadRepository;
    private final ICustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> getAllLeads(Pageable pageable) {
        Page<Lead> leads = leadRepository.findAll(pageable);
        return leads.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadResponse getLeadById(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", "id", id));
        return toResponse(lead);
    }

    @Override
    @Transactional
    public LeadResponse createLead(LeadRequest request) {
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        Optional<Customer> existingCustomer = customerRepository.findByPhone(request.getPhone());
        boolean isExistingCustomer = existingCustomer.isPresent();

        Lead lead = Lead.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .note(request.getNote())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        lead = leadRepository.save(lead);

        LeadResponse response = toResponse(lead);
        response.setIsExistingCustomer(isExistingCustomer);

        if (isExistingCustomer) {
            Customer customer = existingCustomer.get();
            response.setCustomerId(customer.getCustomerId());
            if (customer.getTier() != null) {
                response.setTierCode(customer.getTier().getCode().name());
                response.setTierName(customer.getTier().getCode().getDescription());
            }
        }

        log.info("Created new lead: {} for {} customer", lead.getLeadId(),
                isExistingCustomer ? "existing" : "new");
        return response;
    }

    @Override
    @Transactional
    public LeadResponse updateLead(Long id, LeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", "id", id));

        lead.setFullName(request.getFullName());
        lead.setPhone(request.getPhone());
        lead.setNote(request.getNote());

        lead = leadRepository.save(lead);
        log.info("Updated lead: {}", lead.getLeadId());
        return toResponse(lead);
    }

    @Override
    @Transactional
    public void deleteLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", "id", id));

        if (lead.getStatus() != LeadStatus.NEW) {
            throw new BadRequestException("Can only delete leads with NEW status");
        }

        leadRepository.deleteById(id);
        log.info("Deleted lead: {}", id);
    }

    @Override
    @Transactional
    public LeadResponse updateLeadStatus(Long id, LeadStatusRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", "id", id));

        validateStatusTransition(lead.getStatus(), request.getStatus());
        lead.setStatus(request.getStatus());

        if (request.getNote() != null) {
            String existingNote = lead.getNote() != null ? lead.getNote() + "\n" : "";
            lead.setNote(existingNote + "[Status Update] " + request.getNote());
        }

        lead = leadRepository.save(lead);
        log.info("Updated lead status: {} to {}", lead.getLeadId(), request.getStatus());
        return toResponse(lead);
    }

    private void validateStatusTransition(LeadStatus currentStatus, LeadStatus newStatus) {
        boolean isValid = switch (currentStatus) {
            case NEW -> newStatus == LeadStatus.IN_PROGRESS || newStatus == LeadStatus.LOST;
            case IN_PROGRESS -> newStatus == LeadStatus.WON || newStatus == LeadStatus.LOST;
            case WON, LOST -> false;
        };

        if (!isValid) {
            throw new BadRequestException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }
    }

    private LeadResponse toResponse(Lead lead) {
        return modelMapper.map(lead, LeadResponse.class);
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Could not get client IP address", e);
        }
        return "unknown";
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.warn("Could not get User-Agent", e);
        }
        return "unknown";
    }
}
