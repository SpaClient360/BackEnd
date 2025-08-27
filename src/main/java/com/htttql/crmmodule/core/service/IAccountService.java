package com.htttql.crmmodule.core.service;

import com.htttql.crmmodule.core.dto.AccountDto;
import com.htttql.crmmodule.core.dto.CreateAccountRequest;
import org.springframework.data.domain.Page;

/**
 * Interface for Account service operations
 */
public interface IAccountService {

    /**
     * Get all accounts with pagination
     */
    Page<AccountDto> getAllAccounts(int page, int size);

    /**
     * Get account by ID
     */
    AccountDto getAccountById(Long id);

    /**
     * Get account by username
     */
    AccountDto getAccountByUsername(String username);

    /**
     * Create new account
     */
    AccountDto createAccount(CreateAccountRequest request);

    /**
     * Update account
     */
    AccountDto updateAccount(Long id, CreateAccountRequest request);

    /**
     * Delete account
     */
    void deleteAccount(Long id);

    /**
     * Check if account exists by username
     */
    boolean existsByUsername(String username);

    /**
     * Check if account exists by email
     */
    boolean existsByEmail(String email);
}
