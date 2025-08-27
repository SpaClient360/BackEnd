package com.htttql.crmmodule.core.repository;

import com.htttql.crmmodule.common.enums.TierCode;
import com.htttql.crmmodule.core.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Tier entity
 */
@Repository
public interface TierRepository extends JpaRepository<Tier, Long> {

    Optional<Tier> findByCode(TierCode code);

    @Query("SELECT t FROM Tier t WHERE t.code = 'REGULAR'")
    Optional<Tier> findByCode(String code);

    boolean existsByCode(TierCode code);
}
