package com.example.parkingapp.data.dto

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val phone: String
)
