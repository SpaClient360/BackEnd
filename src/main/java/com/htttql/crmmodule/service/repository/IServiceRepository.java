package com.htttql.crmmodule.service.repository;

import com.htttql.crmmodule.common.enums.ServiceCategory;
import com.htttql.crmmodule.service.entity.SpaService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IServiceRepository extends JpaRepository<SpaService, Long> {

    List<SpaService> findByCategory(ServiceCategory category);

    List<SpaService> findByIsActiveTrue();

    List<SpaService> findByIsActive(Boolean isActive);

    @Query("SELECT s FROM SpaService s WHERE " +
            "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:category IS NULL OR s.category = :category)")
    List<SpaService> searchServices(@Param("search") String search, @Param("category") ServiceCategory category);
}
