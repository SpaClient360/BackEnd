package com.htttql.crmmodule.core.repository;

import com.htttql.crmmodule.core.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    boolean existsByCode(String code);
}
