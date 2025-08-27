package com.htttql.crmmodule.lead.service;

import com.htttql.crmmodule.lead.dto.CreateLeadRequest;
import com.htttql.crmmodule.lead.dto.CreateLeadResponse;
import com.htttql.crmmodule.lead.dto.LeadDto;
import com.htttql.crmmodule.lead.dto.UpdateLeadStatusRequest;
import org.springframework.data.domain.Page;

/**
 * Interface for Lead service operations
 */
public interface ILeadService {

    /**
     * Get all leads with pagination
     */
    Page<LeadDto> getAllLeads(int page, int size);

    /**
     * Get lead by ID
     */
    LeadDto getLeadById(Long id);

    /**
     * Create new lead
     */
    CreateLeadResponse createLead(CreateLeadRequest request);

    /**
     * Check if customer exists by phone number
     */
    boolean isExistingCustomer(String phone);

    /**
     * Update lead status
     */
    LeadDto updateLeadStatus(Long id, UpdateLeadStatusRequest request);

    /**
     * Delete lead
     */
    void deleteLead(Long id);
}
