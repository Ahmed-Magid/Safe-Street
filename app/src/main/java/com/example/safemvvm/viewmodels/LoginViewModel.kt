package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.LoginUser
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.models.User
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class LoginViewModel(private val repository: Repository): ViewModel() {
    val loginResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()

    fun login(loginUser: LoginUser){
        viewModelScope.launch {
            val response = repository.login(loginUser)
            loginResponse.value = response
        }
    }
}