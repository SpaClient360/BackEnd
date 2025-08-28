package com.htttql.crmmodule.core.service;

import com.htttql.crmmodule.core.dto.StaffUserRequest;
import com.htttql.crmmodule.core.dto.StaffUserResponse;
import com.htttql.crmmodule.core.dto.StaffUserStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IStaffUserService {

    Page<StaffUserResponse> getAllStaffUsers(Pageable pageable);

    StaffUserResponse getStaffUserById(Long id);

    StaffUserResponse createStaffUser(StaffUserRequest request);

    StaffUserResponse updateStaffUser(Long id, StaffUserRequest request);

    void deleteStaffUser(Long id);

    StaffUserResponse updateStaffUserStatus(Long id, StaffUserStatusRequest request);
}
