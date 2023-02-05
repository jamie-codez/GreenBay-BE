package com.greenbay.api.utils

enum class Collections(private val value: String) {

    ADMIN_TBL("admins"),
    USER_TBL("users"),
    ROLE_TBL("roles"),
    HOUSE_TBL("houses"),
    TENANTS("tenants"),
    PAYMENTS("payments"),
    TRANSACTIONS("transactions"),
    COMMUNICATIONS("communications"),
    COMPLAINTS("complaints"),
    TASKS("tasks"),
    SCHEDULE("schedules"),
    REFRESH_TOKEN_TBL("refresh_token");

    override fun toString(): String {
        return value
    }
}