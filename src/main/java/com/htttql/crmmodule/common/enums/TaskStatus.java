package com.htttql.crmmodule.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskStatus {
    OPEN("Open"),
    DONE("Done"),
    CANCELLED("Cancelled");

    private final String description;
}
