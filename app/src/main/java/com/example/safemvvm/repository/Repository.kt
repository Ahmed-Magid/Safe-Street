package com.example.safemvvm.repository

import com.example.safemvvm.api.RetrofitInstanceFlask
import com.example.safemvvm.api.RetrofitInstanceSpring
import com.example.safemvvm.models.AddContactBody
import com.example.safemvvm.models.EndTripBody
import com.example.safemvvm.models.IdBody
import com.example.safemvvm.models.LoginUser
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.models.Report
import com.example.safemvvm.models.Trip
import com.example.safemvvm.models.User
import okhttp3.MultipartBody
import retrofit2.Response

class Repository {
    suspend fun register(user: User): Response<MainResponse> {
        return RetrofitInstanceSpring.api.register(user)
    }

    suspend fun login(loginUser: LoginUser): Response<MainResponse>{
        return  RetrofitInstanceSpring.api.login(loginUser)
    }

    suspend fun checkToken(token:String, id: Int): Response<MainResponse>{
        return  RetrofitInstanceSpring.api.checkToken(token, id)
    }

    suspend fun addTrip(token: String, trip: Trip): Response<MainResponse> {
        return RetrofitInstanceSpring.api.addTrip(token, trip)
    }

    suspend fun logout(token:String, idBody: IdBody): Response<MainResponse>{
        return  RetrofitInstanceSpring.api.logout(token, idBody)
    }
    suspend fun getNumOfTrusted(token:String, id:Int): Response<MainResponse>{
        return  RetrofitInstanceSpring.api.getNumOfTrusted(token, id)
    }
    suspend fun getPersonalInfo(token:String, id:Int): Response<MainResponse>{
        return  RetrofitInstanceSpring.api.getPersonalInfo(token, id)
    }

    suspend fun addTrustedContact(token:String, addContactBody: AddContactBody): Response<MainResponse>{
        return  RetrofitInstanceSpring.api.addTrustedContact(token, addContactBody)
    }

    suspend fun addReport(token:String, report: Report): Response<MainResponse> {
        return  RetrofitInstanceSpring.api.addReport(token, report)
    }

    suspend fun train(records: List<MultipartBody.Part>, userId: Int): Response<Unit> {
        return RetrofitInstanceFlask.api.train(records, userId)
    }
    suspend fun setSaved(token: String,saved: Int, userId: Int): Response<MainResponse> {
        return RetrofitInstanceSpring.api.setSaved(token,saved, userId)
    }

    suspend fun predict(record: MultipartBody.Part, userId: Int): Response<Boolean> {
        return RetrofitInstanceFlask.api.predict(record, userId)
    }

    suspend fun getAllTrusted(token:String, id:Int): Response<MainResponse>{
        return  RetrofitInstanceSpring.api.getAllTrusted(token, id)
    }

    suspend fun deleteTrustedContact(token: String, id:Int, email:String): Response<MainResponse>{
        return RetrofitInstanceSpring.api.deleteTrustedContact(token, id, email)
    }

    //end trip
    suspend fun endTrip(token: String, endTripBody: EndTripBody): Response<MainResponse>{
        return RetrofitInstanceSpring.api.endTrip(token, endTripBody)
    }

    suspend fun cancelTrip(token: String, id:Int, tripId:Int): Response<MainResponse>{
        return RetrofitInstanceSpring.api.cancelTrip(token, id, tripId)
    }

    suspend fun checkIngoingTrip(token:String, id:Int): Response<MainResponse>{
        return  RetrofitInstanceSpring.api.checkIngoingTrip(token, id)
    }
}