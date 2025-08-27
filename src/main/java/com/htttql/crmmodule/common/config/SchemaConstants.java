package com.htttql.crmmodule.common.config;

/**
 * Schema constants for multi-schema architecture
 * Each module has its own schema for better separation
 */
public class SchemaConstants {
    public static final String CORE_SCHEMA = "core";
    public static final String LEAD_SCHEMA = "lead";
    public static final String SERVICE_SCHEMA = "service";
    public static final String BILLING_SCHEMA = "billing";
    public static final String AUDIT_SCHEMA = "audit";

    private SchemaConstants() {
        // Private constructor to prevent instantiation
    }
}
