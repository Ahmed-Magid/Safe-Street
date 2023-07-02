package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.IdBody
import com.example.safemvvm.models.LoginUser
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class HomeViewModel(private val repository: Repository): ViewModel() {
    val logoutResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()
    val numOfContactsResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()
    val checkIngoingTripResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()


    fun logout(token : String, idBody: IdBody){
        viewModelScope.launch {
            val response = repository.logout(token, idBody)
            logoutResponse.value = response
        }
    }
    fun getNumOfTrusted(token:String , id: Int){
        viewModelScope.launch {
            val response = repository.getNumOfTrusted(token, id)
            numOfContactsResponse.value = response
        }
    }
    fun checkIngoingTrip(token: String, id : Int){
        viewModelScope.launch {
            val response = repository.checkIngoingTrip(token, id)
            checkIngoingTripResponse.value = response
        }
    }
}