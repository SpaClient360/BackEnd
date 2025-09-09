package com.htttql.crmmodule.core.service;

import com.htttql.crmmodule.common.exception.BadRequestException;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import com.htttql.crmmodule.core.dto.StaffUserRequest;
import com.htttql.crmmodule.core.dto.StaffUserResponse;
import com.htttql.crmmodule.core.dto.StaffUserStatusRequest;
import com.htttql.crmmodule.core.entity.Role;
import com.htttql.crmmodule.core.entity.StaffUser;
import com.htttql.crmmodule.core.repository.IRoleRepository;
import com.htttql.crmmodule.core.repository.IStaffUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("staffUserService")
@RequiredArgsConstructor
public class StaffUserServiceImpl implements IStaffUserService {

    private final IStaffUserRepository staffUserRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<StaffUserResponse> getAllStaffUsers(Pageable pageable) {
        Page<StaffUser> staffUsers = staffUserRepository.findAll(pageable);
        return staffUsers.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffUserResponse getStaffUserById(Long id) {
        StaffUser staffUser = staffUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StaffUser", "id", id));
        return toResponse(staffUser);
    }

    @Override
    @Transactional
    public StaffUserResponse createStaffUser(StaffUserRequest request) {
        if (staffUserRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number already exists");
        }
        if (request.getEmail() != null && staffUserRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        Role role = roleRepository.findByCode(request.getRole().toUpperCase())
                .orElseThrow(() -> new BadRequestException("Invalid role"));

        StaffUser staffUser = StaffUser.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status(request.getStatus() != null ? request.getStatus()
                        : com.htttql.crmmodule.common.enums.StaffStatus.ACTIVE)
                .build();

        staffUser = staffUserRepository.save(staffUser);
        return toResponse(staffUser);
    }

    @Override
    @Transactional
    public StaffUserResponse updateStaffUser(Long id, StaffUserRequest request) {
        StaffUser staffUser = staffUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StaffUser", "id", id));

        if (request.getPhone() != null && !request.getPhone().equals(staffUser.getPhone())) {
            if (staffUserRepository.existsByPhone(request.getPhone())) {
                throw new BadRequestException("Phone number already exists");
            }
        }
        if (request.getEmail() != null && !request.getEmail().equals(staffUser.getEmail())) {
            if (staffUserRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
        }

        if (request.getFullName() != null)
            staffUser.setFullName(request.getFullName());
        if (request.getPhone() != null)
            staffUser.setPhone(request.getPhone());
        if (request.getEmail() != null)
            staffUser.setEmail(request.getEmail());
        if (request.getPassword() != null)
            staffUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        if (request.getStatus() != null)
            staffUser.setStatus(request.getStatus());

        if (request.getRole() != null) {
            Role role = roleRepository.findByCode(request.getRole().toUpperCase())
                    .orElseThrow(() -> new BadRequestException("Invalid role"));
            staffUser.setRole(role);
        }

        staffUser = staffUserRepository.save(staffUser);
        return toResponse(staffUser);
    }

    @Override
    @Transactional
    public void deleteStaffUser(Long id) {
        if (!staffUserRepository.existsById(id)) {
            throw new ResourceNotFoundException("StaffUser", "id", id);
        }
        staffUserRepository.deleteById(id);
    }

    @Override
    @Transactional
    public StaffUserResponse updateStaffUserStatus(Long id, StaffUserStatusRequest request) {
        StaffUser staffUser = staffUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StaffUser", "id", id));

        staffUser.setStatus(request.getStatus());
        staffUser = staffUserRepository.save(staffUser);
        return toResponse(staffUser);
    }

    private StaffUserResponse toResponse(StaffUser staffUser) {
        StaffUserResponse response = modelMapper.map(staffUser, StaffUserResponse.class);
        response.setRole(staffUser.getRole().getCode());
        return response;
    }
}
