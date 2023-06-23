package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.models.User
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class RegistrationViewModel(private val repository: Repository): ViewModel() {
    val registerResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()

    fun register(user: User){
        viewModelScope.launch {
            val response = repository.register(user)
            registerResponse.value = response
        }
    }

}