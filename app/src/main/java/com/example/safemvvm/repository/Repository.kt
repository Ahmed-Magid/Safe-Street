package com.example.safemvvm.repository

import com.example.apitrial.api.RetrofitInstance
import com.example.safemvvm.models.LoginUser
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.models.User
import retrofit2.Response

class Repository {
    suspend fun register(user: User): Response<MainResponse> {
        return RetrofitInstance.api.register(user)
    }

    suspend fun login(loginUser: LoginUser): Response<MainResponse>{
        return  RetrofitInstance.api.login(loginUser)
    }
}