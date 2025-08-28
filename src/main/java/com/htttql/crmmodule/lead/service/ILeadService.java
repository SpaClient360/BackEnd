package com.htttql.crmmodule.lead.service;

import com.htttql.crmmodule.lead.dto.LeadRequest;
import com.htttql.crmmodule.lead.dto.LeadResponse;
import com.htttql.crmmodule.lead.dto.LeadStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ILeadService {

    Page<LeadResponse> getAllLeads(Pageable pageable);

    LeadResponse getLeadById(Long id);

    LeadResponse createLead(LeadRequest request);

    LeadResponse updateLead(Long id, LeadRequest request);

    void deleteLead(Long id);

    LeadResponse updateLeadStatus(Long id, LeadStatusRequest request);
}
