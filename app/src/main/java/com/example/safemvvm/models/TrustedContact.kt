package com.example.safemvvm.models

import androidx.room.PrimaryKey


data class TrustedContact(
    val firstName: String,
    val lastName: String,
    val email: String
    //val id: Int? = null
)

