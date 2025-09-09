package com.htttql.crmmodule.lead.service;

import com.htttql.crmmodule.common.enums.LeadStatus;
import com.htttql.crmmodule.lead.dto.LeadRequest;
import com.htttql.crmmodule.lead.dto.LeadResponse;
import com.htttql.crmmodule.lead.dto.LeadStatusRequest;
import com.htttql.crmmodule.lead.dto.LeadStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ILeadService {

    Page<LeadResponse> getAllLeads(Pageable pageable);

    LeadResponse getLeadById(Long id);

    LeadResponse createLead(LeadRequest request);

    LeadResponse updateLead(Long id, LeadRequest request);

    void deleteLead(Long id);

    LeadResponse updateLeadStatus(Long id, LeadStatusRequest request);

    LeadStats getLeadStats();

    List<LeadResponse> getLeadsByStatus(LeadStatus status);

    Page<LeadResponse> getLeadsByStatus(LeadStatus status, Pageable pageable);
}
