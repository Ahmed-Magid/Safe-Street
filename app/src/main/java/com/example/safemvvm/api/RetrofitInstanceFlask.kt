package com.example.safemvvm.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "" // TODO Deploy Flask
object RetrofitInstanceFlask {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api: FlaskApi by lazy {
        retrofit.create(FlaskApi::class.java)
    }
}