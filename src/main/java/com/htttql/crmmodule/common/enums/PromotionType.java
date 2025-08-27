package com.htttql.crmmodule.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromotionType {
    PERCENT("Percentage discount"),
    AMOUNT("Fixed amount discount");

    private final String description;
}
