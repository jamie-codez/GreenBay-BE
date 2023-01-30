package com.greenbay.api.repos

import com.greenbay.api.domain.RefreshToken

interface RefreshTokenRepo {
    suspend fun save(refreshToken:RefreshToken)
    suspend fun getRefreshToken(email:String,issuer:String)
    suspend fun updateRefreshToken(email: String,replacement:RefreshToken)
    suspend fun deleteRefreshToken(email: String)
}