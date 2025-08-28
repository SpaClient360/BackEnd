package com.htttql.crmmodule.audit.service;

import com.htttql.crmmodule.audit.dto.RetouchScheduleRequest;
import com.htttql.crmmodule.audit.dto.RetouchScheduleResponse;
import com.htttql.crmmodule.audit.entity.RetouchSchedule;
import com.htttql.crmmodule.common.enums.RetouchStatus;
import com.htttql.crmmodule.audit.repository.IRetouchScheduleRepository;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of Retouch Schedule Service
 */
@Service
@RequiredArgsConstructor
public class RetouchScheduleServiceImpl implements IRetouchScheduleService {

    private final IRetouchScheduleRepository retouchScheduleRepository;

    @Override
    public Page<RetouchScheduleResponse> getAllRetouchSchedules(Pageable pageable) {
        Page<RetouchSchedule> schedules = retouchScheduleRepository.findAll(pageable);
        return schedules.map(this::mapToResponse);
    }

    @Override
    public RetouchScheduleResponse createRetouchSchedule(RetouchScheduleRequest request) {
        // Note: This is a simplified implementation
        // In real scenario, you would need to fetch CustomerCase entity
        RetouchSchedule schedule = RetouchSchedule.builder()
                .dueAt(request.getScheduledDate().atStartOfDay())
                .note(request.getNotes())
                .status(RetouchStatus.PLANNED)
                .build();

        RetouchSchedule savedSchedule = retouchScheduleRepository.save(schedule);
        return mapToResponse(savedSchedule);
    }

    @Override
    public RetouchScheduleResponse updateRetouchSchedule(Long id, RetouchScheduleRequest request) {
        RetouchSchedule schedule = retouchScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retouch schedule not found with id: " + id));

        schedule.setDueAt(request.getScheduledDate().atStartOfDay());
        schedule.setNote(request.getNotes());

        RetouchSchedule updatedSchedule = retouchScheduleRepository.save(schedule);
        return mapToResponse(updatedSchedule);
    }

    @Override
    public void deleteRetouchSchedule(Long id) {
        if (!retouchScheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Retouch schedule not found with id: " + id);
        }
        retouchScheduleRepository.deleteById(id);
    }

    @Override
    public RetouchScheduleResponse updateRetouchScheduleStatus(Long id, String status) {
        RetouchSchedule schedule = retouchScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retouch schedule not found with id: " + id));

        RetouchStatus retouchStatus = RetouchStatus.valueOf(status.toUpperCase());
        schedule.setStatus(retouchStatus);

        RetouchSchedule updatedSchedule = retouchScheduleRepository.save(schedule);
        return mapToResponse(updatedSchedule);
    }

    private RetouchScheduleResponse mapToResponse(RetouchSchedule schedule) {
        return RetouchScheduleResponse.builder()
                .scheduleId(schedule.getRetouchId())
                .customerId(schedule.getCustomerCase() != null ? schedule.getCustomerCase().getCaseId() : null)
                .caseId(schedule.getCustomerCase() != null ? schedule.getCustomerCase().getCaseId() : null)
                .scheduledDate(schedule.getDueAt() != null ? schedule.getDueAt().toLocalDate() : null)
                .status(schedule.getStatus())
                .notes(schedule.getNote())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}
