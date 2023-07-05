package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.EndTripBody
import com.example.safemvvm.models.ExtendTripBody
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.models.Trip
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Response
import java.io.Serializable

class WhileInTripViewModel (private val repository: Repository): ViewModel() {
    val endTripResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()
    val cancelTripResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()
    val extendTripResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()
    val predictResponse: MutableLiveData<Response<String>> = MutableLiveData()


    //end trip
    fun endTrip(token: String, endTripBody: EndTripBody){
        viewModelScope.launch {
            val response = repository.endTrip(token, endTripBody)
            endTripResponse.value = response
        }
    }

    fun cancelTrip(token: String , id : Int, customerId: Int){
        viewModelScope.launch {
            val response = repository.cancelTrip(token, id, customerId)
            cancelTripResponse.value = response
        }
    }

    fun extendTrip(token: String, extendTripBody: ExtendTripBody) {
        viewModelScope.launch {
            val response = repository.extendTrip(token, extendTripBody)
            extendTripResponse.value = response
        }
    }

    fun predict(record: MultipartBody.Part, userId: Int) {
        viewModelScope.launch {
            val response = repository.predict(record, userId)
            predictResponse.value = response
        }
    }
}