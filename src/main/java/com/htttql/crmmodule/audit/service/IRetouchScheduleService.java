package com.htttql.crmmodule.audit.service;

import com.htttql.crmmodule.audit.dto.RetouchScheduleRequest;
import com.htttql.crmmodule.audit.dto.RetouchScheduleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Retouch Schedule operations
 */
public interface IRetouchScheduleService {

    /**
     * Get all retouch schedules with pagination
     */
    Page<RetouchScheduleResponse> getAllRetouchSchedules(Pageable pageable);

    /**
     * Create new retouch schedule
     */
    RetouchScheduleResponse createRetouchSchedule(RetouchScheduleRequest request);

    /**
     * Update retouch schedule
     */
    RetouchScheduleResponse updateRetouchSchedule(Long id, RetouchScheduleRequest request);

    /**
     * Delete retouch schedule
     */
    void deleteRetouchSchedule(Long id);

    /**
     * Update retouch schedule status
     */
    RetouchScheduleResponse updateRetouchScheduleStatus(Long id, String status);
}
