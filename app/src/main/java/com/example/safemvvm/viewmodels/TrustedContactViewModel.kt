package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.AddContactBody
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.models.User
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class TrustedContactViewModel (private val repository: Repository): ViewModel() {
    val addContactResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()

    fun addTrustedContact(token: String , addContactBody: AddContactBody){
        viewModelScope.launch {
            val response = repository.addTrustedContact(token, addContactBody)
            addContactResponse.value = response
        }
    }

}