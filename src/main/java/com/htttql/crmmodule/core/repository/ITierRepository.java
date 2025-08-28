package com.htttql.crmmodule.core.repository;

import com.htttql.crmmodule.core.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITierRepository extends JpaRepository<Tier, Long> {

    Optional<Tier> findByCode(String code);

    boolean existsByCode(String code);
}
