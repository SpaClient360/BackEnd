package com.htttql.crmmodule.service.service;

import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import com.htttql.crmmodule.service.entity.CasePhoto;
import com.htttql.crmmodule.service.entity.CustomerCase;
import com.htttql.crmmodule.common.enums.PhotoType;
import com.htttql.crmmodule.service.repository.ICasePhotoRepository;
import com.htttql.crmmodule.service.repository.ICustomerCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Simple service implementation for CasePhoto
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CasePhotoServiceImpl implements ICasePhotoService {

    private final ICasePhotoRepository casePhotoRepository;
    private final ICustomerCaseRepository customerCaseRepository;

    private static final String UPLOAD_PATH = "uploads/photos";

    @Override
    public CasePhoto uploadPhoto(Long caseId, PhotoType type, String note, MultipartFile file) {
        log.info("Uploading photo for case ID: {}, type: {}", caseId, type);

        // Validate case exists
        CustomerCase customerCase = customerCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + caseId));

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Photo file cannot be empty");
        }

        // Create photo entity
        CasePhoto casePhoto = CasePhoto.builder()
                .customerCase(customerCase)
                .type(type)
                .note(note)
                .takenAt(LocalDateTime.now())
                .isPublic(false)
                .consentForMarketing(false)
                .anonymized(false)
                .build();

        // Save file and get metadata
        String fileName = savePhotoFile(file);
        casePhoto.setFileName(fileName);
        casePhoto.setFileSize(file.getSize());
        casePhoto.setMimeType(file.getContentType());
        casePhoto.setFileUrl("/api/photos/" + fileName);

        // Save to database
        CasePhoto savedPhoto = casePhotoRepository.save(casePhoto);

        log.info("Photo uploaded successfully with ID: {}", savedPhoto.getPhotoId());
        return savedPhoto;
    }

    @Override
    public List<CasePhoto> getPhotosByCaseId(Long caseId) {
        return casePhotoRepository.findByCustomerCase_CaseId(caseId);
    }

    @Override
    public List<CasePhoto> getPhotosByCaseIdAndType(Long caseId, PhotoType type) {
        return casePhotoRepository.findByCustomerCase_CaseIdAndType(caseId, type);
    }

    @Override
    public void deletePhoto(Long photoId) {
        CasePhoto casePhoto = casePhotoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with ID: " + photoId));

        // Delete file from storage
        deletePhotoFile(casePhoto.getFileName());

        // Delete from database
        casePhotoRepository.delete(casePhoto);
        log.info("Photo deleted successfully with ID: {}", photoId);
    }

    // Private helper methods

    private String savePhotoFile(MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get(UPLOAD_PATH);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            log.info("Photo file saved: {}", filePath);
            return filename;
        } catch (IOException e) {
            log.error("Error saving photo file: {}", e.getMessage());
            throw new RuntimeException("Error saving photo file", e);
        }
    }

    private void deletePhotoFile(String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_PATH, filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Photo file deleted: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Error deleting photo file: {}", e.getMessage());
        }
    }
}
