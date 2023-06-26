package com.example.safemvvm.api

import com.example.safemvvm.models.IdBody
import com.example.safemvvm.models.LoginUser
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SimpleApi {

    @POST("auth/register")
    suspend fun register(@Body user: User): Response<MainResponse>

    @POST("auth/authenticate")
    suspend fun login(@Body loginUser: LoginUser): Response<MainResponse>

    @POST("customer/checkTokenAvailability")
    suspend fun checkToken(@Header("Authorization") token: String, @Body idBody: IdBody): Response<MainResponse>

    @GET("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>

}