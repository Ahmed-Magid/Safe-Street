package com.example.apitrial.api


import com.example.safemvvm.api.SimpleApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

private const val BASE_URL = "https://safe-st.up.railway.app/"
object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api: SimpleApi by lazy {
        retrofit.create(SimpleApi::class.java)
    }
}