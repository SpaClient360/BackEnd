package com.htttql.crmmodule.lead.service;

import com.htttql.crmmodule.lead.dto.AppointmentRequest;
import com.htttql.crmmodule.lead.dto.AppointmentResponse;
import com.htttql.crmmodule.lead.entity.Appointment;
import com.htttql.crmmodule.common.enums.AppointmentStatus;
import com.htttql.crmmodule.lead.repository.IAppointmentRepository;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of Appointment Service
 */
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements IAppointmentService {

    private final IAppointmentRepository appointmentRepository;

    @Override
    public Page<AppointmentResponse> getAllAppointments(Pageable pageable) {
        Page<Appointment> appointments = appointmentRepository.findAll(pageable);
        return appointments.map(this::mapToResponse);
    }

    @Override
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return mapToResponse(appointment);
    }

    @Override
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        Appointment appointment = Appointment.builder()
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .status(request.getStatus())
                .note(request.getNotes())
                // createdAt is handled by BaseEntity
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return mapToResponse(savedAppointment);
    }

    @Override
    public AppointmentResponse updateAppointment(Long id, AppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        appointment.setStartAt(request.getStartAt());
        appointment.setEndAt(request.getEndAt());
        appointment.setStatus(request.getStatus());
        appointment.setNote(request.getNotes());

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        return mapToResponse(updatedAppointment);
    }

    @Override
    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Appointment not found with id: " + id);
        }
        appointmentRepository.deleteById(id);
    }

    @Override
    public AppointmentResponse updateAppointmentStatus(Long id, String status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
        appointment.setStatus(appointmentStatus);

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        return mapToResponse(updatedAppointment);
    }

    @Override
    public Page<AppointmentResponse> getTodayAppointments(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get today's date range
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1).minusNanos(1);

        // Filter appointments for today
        Page<Appointment> appointments = appointmentRepository.findByStartAtBetween(todayStart, todayEnd, pageable);
        return appointments.map(this::mapToResponse);
    }

    @Override
    public Page<AppointmentResponse> getAppointmentsByDateRange(String startDate, String endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // For now, return all appointments
        // In production, you would filter by date range
        Page<Appointment> appointments = appointmentRepository.findAll(pageable);
        return appointments.map(this::mapToResponse);
    }

    @Override
    public Page<AppointmentResponse> getTechnicianAppointments(Long technicianId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // For now, return all appointments
        // In production, you would filter by technician
        Page<Appointment> appointments = appointmentRepository.findAll(pageable);
        return appointments.map(this::mapToResponse);
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .appointmentId(appointment.getApptId())
                .customerId(appointment.getCustomer() != null ? appointment.getCustomer().getCustomerId() : null)
                .serviceId(appointment.getService() != null ? appointment.getService().getServiceId() : null)
                .startAt(appointment.getStartAt())
                .endAt(appointment.getEndAt())
                .status(appointment.getStatus())
                .notes(appointment.getNote())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
