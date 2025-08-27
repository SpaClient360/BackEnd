package com.htttql.crmmodule.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvoiceStatus {
    DRAFT("Draft"),
    UNPAID("Unpaid"),
    PAID("Paid"),
    VOID("Void");

    private final String description;
}
