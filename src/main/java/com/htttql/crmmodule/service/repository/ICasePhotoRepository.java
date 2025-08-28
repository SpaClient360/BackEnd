package com.htttql.crmmodule.service.repository;

import com.htttql.crmmodule.service.entity.CasePhoto;
import com.htttql.crmmodule.common.enums.PhotoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Simple repository interface for CasePhoto entity
 */
@Repository
public interface ICasePhotoRepository extends JpaRepository<CasePhoto, Long> {

    /**
     * Find photos by case ID
     */
    List<CasePhoto> findByCustomerCase_CaseId(Long caseId);

    /**
     * Find photos by case ID and type
     */
    List<CasePhoto> findByCustomerCase_CaseIdAndType(Long caseId, PhotoType type);
}
