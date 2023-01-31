package com.greenbay.api.utils

enum class Collections(private val value: String) {

    ADMIN_TBL("admins"),
    USER_TBL("users"),
    ROLE_TBL("roles"),
    HOUSE_TBL("houses"),
    REFRESH_TOKEN_TBL("refresh_token");

    override fun toString(): String {
        return value
    }
}