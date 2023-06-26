package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.IdBody
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.models.User
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(private val repository: Repository): ViewModel() {
    val tokenCheckResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()

    fun checkToken(token:String , idBody: IdBody){
        viewModelScope.launch {
            val response = repository.checkToken(token, idBody)
            tokenCheckResponse.value = response
        }
    }

}