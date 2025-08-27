package com.htttql.crmmodule.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CASH("Cash"),
    CARD("Credit/Debit Card"),
    BANK("Bank Transfer"),
    EWALLET("E-Wallet");

    private final String description;
}
