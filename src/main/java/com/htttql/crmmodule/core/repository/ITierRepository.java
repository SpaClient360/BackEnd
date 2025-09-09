package com.htttql.crmmodule.core.repository;

import com.htttql.crmmodule.common.enums.TierCode;
import com.htttql.crmmodule.core.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITierRepository extends JpaRepository<Tier, Long> {

    Optional<Tier> findByCode(TierCode code);

    boolean existsByCode(TierCode code);
}
