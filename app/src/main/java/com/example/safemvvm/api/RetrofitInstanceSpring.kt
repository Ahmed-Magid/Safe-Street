package com.example.safemvvm.api


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://safe-st.up.railway.app/"
object RetrofitInstanceSpring {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api: SpringApi by lazy {
        retrofit.create(SpringApi::class.java)
    }
}