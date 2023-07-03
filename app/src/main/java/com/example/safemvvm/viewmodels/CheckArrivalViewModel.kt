package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.EmergencyBody
import com.example.safemvvm.models.EndTripBody
import com.example.safemvvm.models.ExtendTripBody
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class CheckArrivalViewModel (private val repository: Repository): ViewModel() {
    val endTripResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()
    val extendTripResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()
    val fireEmergencyResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()
    fun endTrip(token: String, endTripBody: EndTripBody){
        viewModelScope.launch {
            val response = repository.endTrip(token, endTripBody)
            endTripResponse.value = response
        }
    }

    fun extendTrip(token: String, extendTripBody: ExtendTripBody) {
        viewModelScope.launch {
            val response = repository.extendTrip(token, extendTripBody)
            extendTripResponse.value = response
        }
    }

    fun fireEmergency(token: String, emergencyBody: EmergencyBody){
        viewModelScope.launch {
            val response = repository.fireEmergency(token, emergencyBody)
            fireEmergencyResponse.value = response
        }
    }
}