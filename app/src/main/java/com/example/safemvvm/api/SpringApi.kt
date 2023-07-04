package com.example.safemvvm.api

import com.example.safemvvm.models.AddContactBody
import com.example.safemvvm.models.EmergencyBody
import com.example.safemvvm.models.EndTripBody
import com.example.safemvvm.models.ExtendTripBody
import com.example.safemvvm.models.IdBody
import com.example.safemvvm.models.LoginUser
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.models.Report
import com.example.safemvvm.models.Trip
import com.example.safemvvm.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface SpringApi {

    @POST("auth/register")
    suspend fun register(@Body user: User): Response<MainResponse>

    @POST("auth/authenticate")
    suspend fun login(@Body loginUser: LoginUser): Response<MainResponse>

    @GET("customer/checkTokenAvailability")
    suspend fun checkToken(
        @Header("Authorization") token: String,
        @Query("id") id: Int
    ): Response<MainResponse>

    @POST("trip/addTrip")
    suspend fun addTrip(
        @Header("Authorization") token: String,
        @Body trip: Trip
    ): Response<MainResponse>

    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String,
        @Body idBody: IdBody
    ): Response<MainResponse>

    ///////////////////////////
    @GET("customer/getNumOfTrusted")
    suspend fun getNumOfTrusted(
        @Header("Authorization") token: String,
        @Query("id") id: Int
    ): Response<MainResponse>

    @GET("customer/getPersonalInfo")
    suspend fun getPersonalInfo(
        @Header("Authorization") token: String,
        @Query("id") id: Int
    ): Response<MainResponse>

    @POST("customer/addTrustedContact")
    suspend fun addTrustedContact(
        @Header("Authorization") token: String,
        @Body addContactBody: AddContactBody
    ): Response<MainResponse>

    @POST("report/addReport")
    suspend fun addReport(
        @Header("Authorization") token: String,
        @Body report: Report
    ): Response<MainResponse>

    //////////////////////////////
    @GET("customer/getAllTrusted")
    suspend fun getAllTrusted(
        @Header("Authorization") token: String,
        @Query("id") id: Int
    ): Response<MainResponse>

    @DELETE("customer/deleteTrustedContact")
    suspend fun deleteTrustedContact(
        @Header("Authorization") token: String,
        @Query("id") id: Int,
        @Query("email") email: String
    ): Response<MainResponse>

    //////////////////////////////
    @PUT("trip/endTrip")
    suspend fun endTrip(
        @Header("Authorization") token: String,
        @Body endTripBody: EndTripBody
    ): Response<MainResponse>

    @DELETE("trip/cancelTrip")
    suspend fun cancelTrip(
        @Header("Authorization") token: String,
        @Query("id") id: Int,
        @Query("customerId") customerId: Int
    ): Response<MainResponse>

    @PUT("trip/extendTrip")
    suspend fun extendTrip(
        @Header("Authorization") token: String,
        @Body extendTripBody: ExtendTripBody
    ): Response<MainResponse>

    @GET("trip/checkIngoingTrip")
    suspend fun checkIngoingTrip(
        @Header("Authorization") token: String,
        @Query("id") id: Int
    ): Response<MainResponse>

    @GET("report/listLocationReports")
    suspend fun getLocationReports(
        @Header("Authorization") token: String,
        @Query("id") id: Int,
        @Query("longitude") longitude: String,
        @Query("latitude") latitude: String
    ): Response<MainResponse>

    @GET("report/listAllLocationWithScore")
    suspend fun getAllLocationsWithScore(@Header("Authorization") token: String, @Query("id") id: Int): Response<MainResponse>

    @POST("emergency/fireEmergency")
    suspend fun fireEmergency(
        @Header("Authorization") token: String,
        @Body emergencyBody: EmergencyBody
    ): Response<MainResponse>

    @POST("customer/setVoice")
    suspend fun setSaved(@Header("Authorization") token: String,@Query("saved") saved: Int,@Query("id") id: Int): Response<MainResponse>
}