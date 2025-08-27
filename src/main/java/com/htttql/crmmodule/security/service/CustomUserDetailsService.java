package com.htttql.crmmodule.security.service;

import com.htttql.crmmodule.core.entity.StaffUser;
import com.htttql.crmmodule.core.repository.StaffUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Custom UserDetailsService implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final StaffUserRepository staffUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Username can be email or phone
        StaffUser staffUser = staffUserRepository.findByEmailOrPhone(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return createUserPrincipal(staffUser);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        StaffUser staffUser = staffUserRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return createUserPrincipal(staffUser);
    }

    private UserPrincipal createUserPrincipal(StaffUser staffUser) {
        return UserPrincipal.builder()
                .id(staffUser.getStaffId())
                .fullName(staffUser.getFullName())
                .username(staffUser.getEmail() != null ? staffUser.getEmail() : staffUser.getPhone())
                .email(staffUser.getEmail())
                .phone(staffUser.getPhone())
                .password(staffUser.getPasswordHash())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + staffUser.getRole().getCode())))
                .isEnabled(staffUser.getStatus().name().equals("ACTIVE"))
                .build();
    }
}
