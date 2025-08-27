package com.htttql.crmmodule.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeadStatus {
    NEW("New lead"),
    IN_PROGRESS("In progress"),
    WON("Won - converted to customer"),
    LOST("Lost - did not convert");

    private final String description;
}
