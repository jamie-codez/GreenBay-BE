package com.greenbay.api.repos

import com.greenbay.api.domain.AppUser

interface UserRepo {
    suspend fun saveUser(appUser: AppUser)
    suspend fun getAppUsers(pageNumber:Int,pageSize:Int)
    suspend fun updateAppUser(email:String,appUser: AppUser)
    suspend fun deleteAppUser(appUser: AppUser)
}