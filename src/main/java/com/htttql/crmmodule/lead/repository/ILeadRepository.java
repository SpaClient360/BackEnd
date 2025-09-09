package com.htttql.crmmodule.lead.repository;

import com.htttql.crmmodule.common.enums.LeadStatus;
import com.htttql.crmmodule.lead.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ILeadRepository extends JpaRepository<Lead, Long> {

    List<Lead> findByStatus(LeadStatus status);

    @Query("SELECT l FROM Lead l WHERE l.status = :status ORDER BY l.createdAt DESC")
    Page<Lead> findByStatusWithPagination(@Param("status") LeadStatus status, Pageable pageable);
}
