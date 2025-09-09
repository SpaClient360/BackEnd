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

        CustomerCase customerCase = customerCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + caseId));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Photo file cannot be empty");
        }
        CasePhoto casePhoto = CasePhoto.builder()
                .customerCase(customerCase)
                .type(type)
                .note(note)
                .takenAt(LocalDateTime.now())
                .isPublic(false)
                .consentForMarketing(false)
                .anonymized(false)
                .build();

        String fileName = savePhotoFile(file);
        casePhoto.setFileName(fileName);
        casePhoto.setFileSize(file.getSize());
        casePhoto.setMimeType(file.getContentType());
        casePhoto.setFileUrl("/api/photos/" + fileName);

        CasePhoto savedPhoto = casePhotoRepository.save(casePhoto);
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

        deletePhotoFile(casePhoto.getFileName());
        casePhotoRepository.delete(casePhoto);
    }

    private String savePhotoFile(MultipartFile file) {
        try {
            Path uploadDir = Paths.get(UPLOAD_PATH);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Error saving photo file", e);
        }
    }

    private void deletePhotoFile(String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_PATH, filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
        }
    }
}
