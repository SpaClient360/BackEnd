package com.htttql.crmmodule.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RetouchStatus {
    PLANNED("Planned"),
    DONE("Done"),
    MISSED("Missed");

    private final String description;
}
