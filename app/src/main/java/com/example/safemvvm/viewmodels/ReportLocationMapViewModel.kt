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

class ReportLocationMapViewModel(private val repository: Repository): ViewModel() {
    val getAllLocationsWithScoreResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()

    fun getAllLocationsWithScore(token: String, id: Int) {
        viewModelScope.launch {
            val response = repository.getAllLocationsWithScore(token, id)
            getAllLocationsWithScoreResponse.value = response
        }
    }

}