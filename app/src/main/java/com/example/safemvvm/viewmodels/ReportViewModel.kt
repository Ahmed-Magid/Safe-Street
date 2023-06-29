package com.example.safemvvm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.models.Report
import com.example.safemvvm.models.User
import com.example.safemvvm.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class ReportViewModel(private val repository: Repository): ViewModel() {
    val addReportResponse: MutableLiveData<Response<MainResponse>> = MutableLiveData()

    fun addReport(token: String, report: Report){
        viewModelScope.launch {
            val response = repository.addReport(token, report)
            addReportResponse.value = response
        }
    }

}