package com.example.safemvvm.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.repository.Repository

class CreateTripViewModelFactory (private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateTripViewModel(repository) as T
    }
}