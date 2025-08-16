package com.htttql.crmmodule.user.service;

import com.htttql.crmmodule.user.dto.UserCreateDTO;
import com.htttql.crmmodule.user.dto.UserDTO;
import com.htttql.crmmodule.user.dto.UserProfileDTO;
import com.htttql.crmmodule.user.entity.User;
import com.htttql.crmmodule.user.enums.Gender;
import com.htttql.crmmodule.user.enums.UserStatus;
import com.htttql.crmmodule.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for User operations (includes both login and staff profile
 * management)
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create new user
     */
    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        // Validate email uniqueness
        if (userCreateDTO.getEmail() != null && userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + userCreateDTO.getEmail());
        }

        // Check if phone already exists
        if (userCreateDTO.getPhone() != null && userRepository.existsByPhone(userCreateDTO.getPhone())) {
            throw new RuntimeException("Phone already exists: " + userCreateDTO.getPhone());
        }

        User user = new User();
        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        user.setEmail(userCreateDTO.getEmail());
        user.setPhone(userCreateDTO.getPhone());
        user.setRoleId(userCreateDTO.getRoleId());
        user.setStatus(UserStatus.ACTIVE);

        // Staff profile information
        user.setFullName(userCreateDTO.getFullName());
        user.setGender(userCreateDTO.getGender());
        user.setDateOfBirth(userCreateDTO.getDateOfBirth());
        user.setPosition(userCreateDTO.getPosition());
        user.setAddress(userCreateDTO.getAddress());
        user.setNote(userCreateDTO.getNote());

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    /**
     * Get user by phone
     */
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .map(this::convertToDTO);
    }

    /**
     * Update user
     */
    @Transactional
    public UserDTO updateUser(Long id, UserCreateDTO userCreateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Update basic info
        if (userCreateDTO.getEmail() != null &&
                !userCreateDTO.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + userCreateDTO.getEmail());
        }

        if (userCreateDTO.getPhone() != null &&
                !userCreateDTO.getPhone().equals(user.getPhone()) &&
                userRepository.existsByPhone(userCreateDTO.getPhone())) {
            throw new RuntimeException("Phone already exists: " + userCreateDTO.getPhone());
        }

        if (userCreateDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        }
        user.setEmail(userCreateDTO.getEmail());
        user.setPhone(userCreateDTO.getPhone());
        user.setRoleId(userCreateDTO.getRoleId());

        // Update staff profile information
        user.setFullName(userCreateDTO.getFullName());
        user.setGender(userCreateDTO.getGender());
        user.setDateOfBirth(userCreateDTO.getDateOfBirth());
        user.setPosition(userCreateDTO.getPosition());
        user.setAddress(userCreateDTO.getAddress());
        user.setNote(userCreateDTO.getNote());

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Get all users with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get users by status
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsersByStatus(UserStatus status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsersByRole(Long roleId, Pageable pageable) {
        return userRepository.findByRoleId(roleId, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Search users by email or phone
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchByEmailOrPhone(keyword, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Update user status
     */
    @Transactional
    public UserDTO updateUserStatus(Long id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setStatus(status);
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    /**
     * Get user profile by email or phone
     * 
     * @param emailOrPhone user identifier
     * @return UserProfileDTO
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(String emailOrPhone) {
        User user = userRepository.findByEmailOrPhone(emailOrPhone)
                .orElseThrow(() -> new RuntimeException("User not found: " + emailOrPhone));

        return convertToProfileDTO(user);
    }

    /**
     * Search users by name containing
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsersByName(String name, Pageable pageable) {
        return userRepository.findByFullNameContainingIgnoreCase(name, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get users by position
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsersByPosition(String position, Pageable pageable) {
        return userRepository.findByPositionContainingIgnoreCase(position, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get users by gender
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsersByGender(Gender gender, Pageable pageable) {
        return userRepository.findByGender(gender, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get users by birth date range
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByBirthDateRange(LocalDate startDate, LocalDate endDate) {
        return userRepository.findByDateOfBirthBetween(startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long inactiveUsers = userRepository.countByStatus(UserStatus.INACTIVE);
        long lockedUsers = userRepository.countByStatus(UserStatus.LOCKED);

        return new UserStatistics(totalUsers, activeUsers, inactiveUsers, lockedUsers);
    }

    /**
     * Convert User entity to UserDTO
     */
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roleId(user.getRoleId())
                .status(user.getStatus())
                .fullName(user.getFullName())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .position(user.getPosition())
                .address(user.getAddress())
                .note(user.getNote())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Convert User entity to UserProfileDTO
     */
    private UserProfileDTO convertToProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .position(user.getPosition())
                .address(user.getAddress())
                .avatar(null) // Will be implemented later when avatar feature is added
                .note(user.getNote())
                .build();
    }

    /**
     * User statistics inner class
     */
    public static class UserStatistics {
        private final long totalUsers;
        private final long activeUsers;
        private final long inactiveUsers;
        private final long lockedUsers;

        public UserStatistics(long totalUsers, long activeUsers, long inactiveUsers, long lockedUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.inactiveUsers = inactiveUsers;
            this.lockedUsers = lockedUsers;
        }

        // Getters
        public long getTotalUsers() {
            return totalUsers;
        }

        public long getActiveUsers() {
            return activeUsers;
        }

        public long getInactiveUsers() {
            return inactiveUsers;
        }

        public long getLockedUsers() {
            return lockedUsers;
        }
    }
}