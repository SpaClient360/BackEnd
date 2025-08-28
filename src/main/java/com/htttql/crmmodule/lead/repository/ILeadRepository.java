package com.htttql.crmmodule.lead.repository;

import com.htttql.crmmodule.lead.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ILeadRepository extends JpaRepository<Lead, Long> {
}
