package com.htttql.crmmodule.core.controller;

import com.htttql.crmmodule.common.dto.ApiResponse;
import com.htttql.crmmodule.core.dto.AccountDto;
import com.htttql.crmmodule.core.dto.CreateAccountRequest;
import com.htttql.crmmodule.core.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Account management controller - Public API
 */
@Tag(name = "Account Management", description = "Public account creation - No authentication required")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Create new account (Public API - No authentication required)")
    @PostMapping
    public ResponseEntity<ApiResponse<AccountDto>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {

        AccountDto account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(account, "Account created successfully"));
    }
}
