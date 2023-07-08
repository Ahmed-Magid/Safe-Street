package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.models.User
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Response

class RegistrationViewModel(private val repository: Repository): ViewModel() {
    val registerResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()
    val trainResponse: MutableLiveData<Response<String>> = MutableLiveData()

    fun register(user: User){
        viewModelScope.launch {
            val response = repository.register(user)
            registerResponse.value = response
        }
    }

    fun train(records: List<MultipartBody.Part>, userId: Int) {
        viewModelScope.launch {
            val response = repository.train(records, userId)
            trainResponse.value = response
        }
    }

}