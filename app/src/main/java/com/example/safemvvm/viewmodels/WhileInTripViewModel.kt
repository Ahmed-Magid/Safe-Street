package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.EndTripBody
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class WhileInTripViewModel (private val repository: Repository): ViewModel() {
    val addContactResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()
    val getContactsResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()
    val deleteContactResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()

    //end trip
    fun endTrip(token: String, endTripBody: EndTripBody){
        viewModelScope.launch {
            val response = repository.endTrip(token, endTripBody)
            getContactsResponse.value = response
        }
    }

}