package com.htttql.crmmodule.user.controller;

import com.htttql.crmmodule.auth.dto.ErrorResponse;
import com.htttql.crmmodule.user.dto.UserCreateDTO;
import com.htttql.crmmodule.user.dto.UserDTO;
import com.htttql.crmmodule.user.dto.UserProfileDTO;
import com.htttql.crmmodule.user.enums.Gender;
import com.htttql.crmmodule.user.enums.UserStatus;
import com.htttql.crmmodule.user.service.UserService;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for User operations
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Create new user
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserDTO createdUser = userService.createUser(userCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Get user by id
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Optional<UserDTO> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get user by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        Optional<UserDTO> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get user by phone
     */
    @GetMapping("/phone/{phone}")
    public ResponseEntity<UserDTO> getUserByPhone(@PathVariable String phone) {
        Optional<UserDTO> user = userService.getUserByPhone(phone);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id,
            @Valid @RequestBody UserCreateDTO userUpdateDTO) {
        UserDTO updatedUser = userService.updateUser(id, userUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Update user status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserDTO> updateUserStatus(@PathVariable Long id,
            @RequestParam UserStatus status) {
        UserDTO updatedUser = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all users with pagination
     */
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(@PageableDefault(size = 10) Pageable pageable) {
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<UserDTO>> getUsersByStatus(@PathVariable UserStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<UserDTO> users = userService.getUsersByStatus(status, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by role
     */
    @GetMapping("/role/{roleId}")
    public ResponseEntity<Page<UserDTO>> getUsersByRole(@PathVariable Long roleId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<UserDTO> users = userService.getUsersByRole(roleId, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Search users by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<Page<UserDTO>> searchUsers(@RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<UserDTO> users = userService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Search users by name
     */
    @GetMapping("/search/name")
    public ResponseEntity<Page<UserDTO>> searchByName(@RequestParam String name,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<UserDTO> users = userService.searchUsersByName(name, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by position
     */
    @GetMapping("/position/{position}")
    public ResponseEntity<Page<UserDTO>> getByPosition(@PathVariable String position,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<UserDTO> users = userService.getUsersByPosition(position, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by gender
     */
    @GetMapping("/gender/{gender}")
    public ResponseEntity<Page<UserDTO>> getByGender(@PathVariable Gender gender,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<UserDTO> users = userService.getUsersByGender(gender, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by birth date range
     */
    @GetMapping("/birth-date-range")
    public ResponseEntity<List<UserDTO>> getByBirthDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<UserDTO> users = userService.getUsersByBirthDateRange(startDate, endDate);
        return ResponseEntity.ok(users);
    }

    /**
     * Get user statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<UserService.UserStatistics> getUserStatistics() {
        UserService.UserStatistics statistics = userService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get current user profile
     * 
     * @return current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                UserProfileDTO profile = userService.getUserProfile(authentication.getName());
                return ResponseEntity.ok(profile);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of("Unauthorized", "Invalid or expired token"));

        } catch (Exception e) {
            log.error("Error getting user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of("Unauthorized", "Invalid or expired token"));
        }
    }

}