package com.htttql.crmmodule.service.service;

import com.htttql.crmmodule.service.entity.CasePhoto;
import com.htttql.crmmodule.common.enums.PhotoType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Simple service interface for CasePhoto
 */
public interface ICasePhotoService {

    /**
     * Upload a new photo
     */
    CasePhoto uploadPhoto(Long caseId, PhotoType type, String note, MultipartFile file);

    /**
     * Get photos by case ID
     */
    List<CasePhoto> getPhotosByCaseId(Long caseId);

    /**
     * Get photos by case ID and type
     */
    List<CasePhoto> getPhotosByCaseIdAndType(Long caseId, PhotoType type);

    /**
     * Delete a photo
     */
    void deletePhoto(Long photoId);
}
