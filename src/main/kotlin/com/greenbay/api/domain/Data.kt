package com.greenbay.api.domain

data class AppUser(
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val idNumber: String,
    val password: String
)

data class Admin(
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val idNumber: String,
    val password: String,
    val roles:List<Role>
)

data class Role(
    val roleName: String,
    val roleDescription: String
)

data class House(
    val houseNumber: String,
    val floor: String,
    val bedRooms: String,
    val rent: String,
    val deposit: String,
    val isOccupied: Boolean = false
)

data class RefreshToken(
    val email: String,
    val token: String,
    val eta: Long,
    val issuer: String
)