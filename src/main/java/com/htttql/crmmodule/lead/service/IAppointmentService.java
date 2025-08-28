package com.htttql.crmmodule.lead.service;

import com.htttql.crmmodule.lead.dto.AppointmentRequest;
import com.htttql.crmmodule.lead.dto.AppointmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Appointment operations
 */
public interface IAppointmentService {

    /**
     * Get all appointments with pagination
     */
    Page<AppointmentResponse> getAllAppointments(Pageable pageable);

    /**
     * Get appointment by ID
     */
    AppointmentResponse getAppointmentById(Long id);

    /**
     * Create new appointment
     */
    AppointmentResponse createAppointment(AppointmentRequest request);

    /**
     * Update appointment
     */
    AppointmentResponse updateAppointment(Long id, AppointmentRequest request);

    /**
     * Delete appointment
     */
    void deleteAppointment(Long id);

    /**
     * Update appointment status
     */
    AppointmentResponse updateAppointmentStatus(Long id, String status);
}
