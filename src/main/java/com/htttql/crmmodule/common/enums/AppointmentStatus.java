package com.htttql.crmmodule.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppointmentStatus {
    SCHEDULED("Scheduled"),
    CONFIRMED("Confirmed"),
    NO_SHOW("No show"),
    DONE("Done"),
    CANCELLED("Cancelled");

    private final String description;
}
