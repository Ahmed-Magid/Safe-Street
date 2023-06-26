package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.LoginUser
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class HomeViewModel(private val repository: Repository): ViewModel() {
    val logoutResponse: MutableLiveData<Response<Unit>> = MutableLiveData()

    fun logout(token : String){
        viewModelScope.launch {
            val response = repository.logout(token)
            logoutResponse.value = response
        }
    }
}