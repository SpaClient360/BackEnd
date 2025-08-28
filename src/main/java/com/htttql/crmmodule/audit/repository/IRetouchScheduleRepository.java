package com.htttql.crmmodule.audit.repository;

import com.htttql.crmmodule.audit.entity.RetouchSchedule;
import com.htttql.crmmodule.common.enums.RetouchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for RetouchSchedule entity
 */
@Repository
public interface IRetouchScheduleRepository extends JpaRepository<RetouchSchedule, Long> {

    /**
     * Find retouch schedules by customer ID through the customer case relationship
     */
    List<RetouchSchedule> findByCustomerCase_Customer_CustomerId(Long customerId);

    /**
     * Find retouch schedules by case ID
     */
    List<RetouchSchedule> findByCustomerCase_CaseId(Long caseId);

    /**
     * Find retouch schedules by status
     */
    List<RetouchSchedule> findByStatus(RetouchStatus status);
}
