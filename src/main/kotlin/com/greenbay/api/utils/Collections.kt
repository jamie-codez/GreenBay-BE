package com.greenbay.api.utils

enum class Collections(private val value: String) {

    ADMIN_TBL("admins"),
    USER_TBL("users"),
    REFRESH_TOKEN_TBL("refresh_token");

    override fun toString(): String {
        return value
    }
}