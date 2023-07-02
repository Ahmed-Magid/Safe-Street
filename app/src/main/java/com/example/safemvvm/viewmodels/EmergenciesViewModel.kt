package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.EmergencyBody
import com.example.safemvvm.models.EndTripBody
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class EmergenciesViewModel (private val repository: Repository): ViewModel() {
    val fireEmergencyResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()

    fun fireEmergency(token: String, emergencyBody: EmergencyBody){
        viewModelScope.launch {
            val response = repository.fireEmergency(token, emergencyBody)
            fireEmergencyResponse.value = response
        }
    }
}