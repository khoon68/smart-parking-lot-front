package com.example.parkingapp.data.dto

data class LoginResponse(
    val token: String,
    val username: String,
    val role: String
)
