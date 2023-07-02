package com.example.safemvvm.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://flask-app.up.railway.app/" // TODO Deploy Flask
object RetrofitInstanceFlask {



    private val retrofit by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100,TimeUnit.SECONDS).build();
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
    val api: FlaskApi by lazy {
        retrofit.create(FlaskApi::class.java)
    }
}