package com.htttql.crmmodule.lead.service;

import com.htttql.crmmodule.common.enums.LeadStatus;
import com.htttql.crmmodule.common.exception.BadRequestException;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import com.htttql.crmmodule.core.entity.Customer;
import com.htttql.crmmodule.core.repository.CustomerRepository;
import com.htttql.crmmodule.lead.dto.CreateLeadRequest;
import com.htttql.crmmodule.lead.dto.CreateLeadResponse;
import com.htttql.crmmodule.lead.dto.LeadDto;
import com.htttql.crmmodule.lead.dto.UpdateLeadStatusRequest;
import com.htttql.crmmodule.lead.entity.Lead;
import com.htttql.crmmodule.lead.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Lead service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeadService implements ILeadService {

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public Page<LeadDto> getAllLeads(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Lead> leads = leadRepository.findAll(pageable);
        return leads.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public LeadDto getLeadById(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", "id", id));
        return toDto(lead);
    }

    @Transactional
    public CreateLeadResponse createLead(CreateLeadRequest request) {
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        // Kiểm tra xem khách hàng đã tồn tại chưa
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

        // Tạo response với thông tin chi tiết
        CreateLeadResponse response = CreateLeadResponse.builder()
                .leadId(lead.getLeadId())
                .fullName(lead.getFullName())
                .phone(lead.getPhone())
                .note(lead.getNote())
                .status(lead.getStatus().name())
                .isExistingCustomer(isExistingCustomer)
                .build();

        if (isExistingCustomer) {
            Customer customer = existingCustomer.get();
            response.setCustomerId(customer.getCustomerId());
            if (customer.getTier() != null) {
                response.setTierCode(customer.getTier().getCode().name());
                response.setTierName(customer.getTier().getCode().getDescription());
            }
            response.setMessage("Lead created for existing customer");
            log.info("Created new lead for EXISTING customer: {} from IP: {}", lead.getLeadId(), ipAddress);
        } else {
            response.setMessage("Lead created for new customer");
            log.info("Created new lead for NEW customer: {} from IP: {}", lead.getLeadId(), ipAddress);
        }

        return response;
    }

    /**
     * Kiểm tra xem khách hàng đã tồn tại trong hệ thống chưa
     * 
     * @param phone Số điện thoại cần kiểm tra
     * @return true nếu khách hàng đã tồn tại, false nếu là khách mới
     */
    @Transactional(readOnly = true)
    public boolean isExistingCustomer(String phone) {
        return customerRepository.existsByPhone(phone);
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

    @Transactional
    public LeadDto updateLeadStatus(Long id, UpdateLeadStatusRequest request) {
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

        return toDto(lead);
    }

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

    private LeadDto toDto(Lead lead) {
        LeadDto dto = modelMapper.map(lead, LeadDto.class);
        return dto;
    }
}