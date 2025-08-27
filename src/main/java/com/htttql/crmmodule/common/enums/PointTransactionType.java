package com.htttql.crmmodule.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointTransactionType {
    EARN("Earn points"),
    REDEEM("Redeem points"),
    ADJUST("Manual adjustment");

    private final String description;
}
