package com.greenbay.api.repos

import com.greenbay.api.domain.AppUser

interface AdminRepo {
    suspend fun addAdmin(appUser: AppUser)
    suspend fun getAdmins(page:Int,pageSize:Int)
    suspend fun updateAdmin(email:String,appUser: AppUser)
    suspend fun deleteAdmin(email: String)
}