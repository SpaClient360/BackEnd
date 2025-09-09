package com.htttql.crmmodule.lead.repository;

import com.htttql.crmmodule.lead.entity.Appointment;
import com.htttql.crmmodule.common.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Appointment entity
 */
@Repository
public interface IAppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Find appointments by customer ID
     */
    List<Appointment> findByCustomer_CustomerId(Long customerId);

    /**
     * Find appointments by status
     */
    List<Appointment> findByStatus(AppointmentStatus status);

    /**
     * Find appointments by service ID
     */
    List<Appointment> findByService_ServiceId(Long serviceId);

    /**
     * Find appointments for today
     */
    Page<Appointment> findByStartAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
