package com.example.safemvvm.models

data class User(
    var firstname: String,
    var lastname: String,
    var password: String,
    var confirmationPassword: String,
    var phoneNumber: String,
    var email: String
)
