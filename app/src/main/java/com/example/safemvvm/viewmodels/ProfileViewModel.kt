package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class ProfileViewModel(private val repository: Repository): ViewModel() {
    val profileResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()

    fun getPersonalInfo(token:String ,id: Int){
        viewModelScope.launch {
            val response = repository.getPersonalInfo(token, id)
            profileResponse.value = response
        }
    }

}