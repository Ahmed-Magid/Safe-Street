package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.IdBody
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.models.Trip
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class CreateTripViewModel(private val repository: Repository): ViewModel() {
    val addTripResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()
    val getAllLocationsWithScoreResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()

    fun addTrip(token : String, trip: Trip){
        viewModelScope.launch {
            val response = repository.addTrip(token, trip)
            addTripResponse.value = response
        }
    }

    fun getAllLocationsWithScore(token: String, id: Int) {
        viewModelScope.launch {
            val response = repository.getAllLocationsWithScore(token, id)
            getAllLocationsWithScoreResponse.value = response
        }
    }
}