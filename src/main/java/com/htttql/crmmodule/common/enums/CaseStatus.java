package com.htttql.crmmodule.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseStatus {
    INTAKE("Intake - initial assessment"),
    IN_PROGRESS("In progress"),
    DONE("Done"),
    FOLLOW_UP("Follow up required");

    private final String description;
}
