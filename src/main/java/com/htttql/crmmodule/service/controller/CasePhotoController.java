package com.htttql.crmmodule.service.controller;

import com.htttql.crmmodule.common.dto.ApiResponse;
import com.htttql.crmmodule.common.enums.PhotoType;
import com.htttql.crmmodule.service.entity.CasePhoto;
import com.htttql.crmmodule.service.service.ICasePhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Simple REST Controller for CasePhoto management
 */
@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Case Photo Management", description = "Simple APIs for managing before/after photos")
public class CasePhotoController {

    private final ICasePhotoService casePhotoService;
    private static final String UPLOAD_PATH = "uploads/photos";

    @PostMapping("/upload")
    @Operation(summary = "Upload a new photo", description = "Upload a before or after photo for a customer case")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<CasePhoto>> uploadPhoto(
            @RequestParam("caseId") Long caseId,
            @RequestParam("type") PhotoType type,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam("photoFile") MultipartFile photoFile) {

        log.info("Uploading photo for case ID: {}, type: {}", caseId, type);

        CasePhoto photo = casePhotoService.uploadPhoto(caseId, type, note, photoFile);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(photo, "Photo uploaded successfully"));
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get all photos for a case", description = "Retrieve all photos for a specific case")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<List<CasePhoto>>> getPhotosByCaseId(
            @PathVariable Long caseId) {

        List<CasePhoto> photos = casePhotoService.getPhotosByCaseId(caseId);
        return ResponseEntity.ok(ApiResponse.success(photos, "Photos retrieved successfully"));
    }

    @GetMapping("/case/{caseId}/type/{type}")
    @Operation(summary = "Get photos by case ID and type", description = "Retrieve photos by type (BEFORE or AFTER)")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<List<CasePhoto>>> getPhotosByCaseIdAndType(
            @PathVariable Long caseId,
            @PathVariable PhotoType type) {

        List<CasePhoto> photos = casePhotoService.getPhotosByCaseIdAndType(caseId, type);
        return ResponseEntity.ok(ApiResponse.success(photos, "Photos retrieved successfully"));
    }

    @GetMapping("/{photoId}/file")
    @Operation(summary = "Download photo file", description = "Download the actual photo file")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    public ResponseEntity<ByteArrayResource> downloadPhotoFile(
            @PathVariable Long photoId) {

        try {
            // Get photo info
            CasePhoto photo = casePhotoService.getPhotosByCaseId(photoId).get(0);

            // Read file from storage
            Path filePath = Paths.get(UPLOAD_PATH, photo.getFileName());
            byte[] fileContent = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + photo.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            log.error("Error reading photo file: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{photoId}")
    @Operation(summary = "Delete photo", description = "Delete a photo and its associated file")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @PathVariable Long photoId) {

        casePhotoService.deletePhoto(photoId);
        return ResponseEntity.ok(ApiResponse.success(null, "Photo deleted successfully"));
    }
}
