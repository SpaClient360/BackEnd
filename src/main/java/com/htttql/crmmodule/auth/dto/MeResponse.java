package com.htttql.crmmodule.auth.dto;

import com.htttql.crmmodule.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Me endpoint response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeResponse {

    private UserDTO user;

    /**
     * Create me response
     */
    public static MeResponse of(UserDTO user) {
        return MeResponse.builder()
                .user(user)
                .build();
    }
}