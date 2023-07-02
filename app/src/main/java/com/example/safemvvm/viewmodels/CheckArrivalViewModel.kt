package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.EndTripBody
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class CheckArrivalViewModel (private val repository: Repository): ViewModel() {
    val endTripResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()
    fun endTrip(token: String, endTripBody: EndTripBody){
        viewModelScope.launch {
            val response = repository.endTrip(token, endTripBody)
            endTripResponse.value = response
        }
    }
}