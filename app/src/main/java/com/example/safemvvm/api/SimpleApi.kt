package com.example.safemvvm.api

import com.example.safemvvm.models.LoginUser
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SimpleApi {

    @POST("auth/register")
    suspend fun register(@Body user: User): Response<MainResponse>

    @POST("auth/authenticate")
    suspend fun login(@Body loginUser: LoginUser): Response<MainResponse>
}