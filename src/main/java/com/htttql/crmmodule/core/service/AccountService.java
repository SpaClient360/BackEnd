package com.htttql.crmmodule.core.service;

import com.htttql.crmmodule.common.exception.BadRequestException;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import com.htttql.crmmodule.core.dto.AccountDto;
import com.htttql.crmmodule.core.dto.CreateAccountRequest;
import com.htttql.crmmodule.core.entity.Role;
import com.htttql.crmmodule.core.entity.StaffUser;
import com.htttql.crmmodule.core.repository.RoleRepository;
import com.htttql.crmmodule.core.repository.StaffUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Account service for public account creation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {

    private final StaffUserRepository staffUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Transactional
    public AccountDto createAccount(CreateAccountRequest request) {
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }

        // Check if phone already exists
        if (staffUserRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number already exists");
        }

        // Check if email already exists
        if (staffUserRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Find role
        Role role = roleRepository.findByCode(request.getRole().toUpperCase())
                .orElseThrow(() -> new BadRequestException("Invalid role"));

        // Create staff user
        StaffUser staffUser = StaffUser.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        staffUser = staffUserRepository.save(staffUser);
        log.info("Created new account: {} with role: {}", staffUser.getStaffId(), request.getRole());

        return toDto(staffUser);
    }

    private AccountDto toDto(StaffUser staffUser) {
        AccountDto dto = modelMapper.map(staffUser, AccountDto.class);
        dto.setRole(staffUser.getRole().getCode());
        return dto;
    }

    @Override
    public Page<AccountDto> getAllAccounts(int page, int size) {
        // TODO: Implement pagination
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public AccountDto getAccountById(Long id) {
        StaffUser staffUser = staffUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        return toDto(staffUser);
    }

    @Override
    public AccountDto getAccountByUsername(String username) {
        // Assuming username is phone or email
        StaffUser staffUser = staffUserRepository.findByPhone(username)
                .orElseGet(() -> staffUserRepository.findByEmail(username)
                        .orElseThrow(() -> new ResourceNotFoundException("Account", "username", username)));
        return toDto(staffUser);
    }

    @Override
    public AccountDto updateAccount(Long id, CreateAccountRequest request) {
        // TODO: Implement update logic
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public void deleteAccount(Long id) {
        if (!staffUserRepository.existsById(id)) {
            throw new ResourceNotFoundException("Account", "id", id);
        }
        staffUserRepository.deleteById(id);
        log.info("Deleted account: {}", id);
    }

    @Override
    public boolean existsByUsername(String username) {
        // Assuming username is phone or email
        return staffUserRepository.existsByPhone(username) || staffUserRepository.existsByEmail(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return staffUserRepository.existsByEmail(email);
    }
}
