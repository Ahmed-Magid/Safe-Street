package com.example.safemvvm.viewmodels

import ViewReportsViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.repository.Repository

class ViewReportsViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ViewReportsViewModel(repository) as T
    }
}