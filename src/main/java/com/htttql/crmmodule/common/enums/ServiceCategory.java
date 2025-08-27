package com.htttql.crmmodule.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceCategory {
    LIP("Lip services"),
    BROW("Brow services"),
    OTHER("Other services");

    private final String description;
}
