package com.example.safemvvm.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface FlaskApi {
    @POST("/train")
    @Multipart
    suspend fun train(
        @Part records: List<MultipartBody.Part>,
        @Query("userId") userId: Int
    ): Response<Unit>

    @POST("/predict")
    @Multipart
    suspend fun predict(
        @Part record: MultipartBody.Part,
        @Query("userId") userId: Int
    ): Response<Boolean>
}