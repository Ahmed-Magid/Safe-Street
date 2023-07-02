package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.AddContactBody
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Response

class VoiceParagraphViewModel(private val repository: Repository) : ViewModel() {
    val trainResponse: MutableLiveData<Response<Unit>> = MutableLiveData()
    val savedResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()

    fun train(records: List<MultipartBody.Part>, userId: Int) {
        viewModelScope.launch {
            val response = repository.train(records, userId)
            trainResponse.value = response
        }
    }
    fun setSaved(token:String , saved: Int, userId: Int) {
        viewModelScope.launch {
            val response = repository.setSaved(token,saved, userId)
            savedResponse.value = response
        }
    }
}
