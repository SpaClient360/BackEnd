package com.htttql.crmmodule.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PhotoType {
    BEFORE("Before service"),
    AFTER("After service");

    private final String description;
}
