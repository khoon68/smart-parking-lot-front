package com.example.parkingapp.data.dto

data class ReservationResponse(
    val reservationId: Int,
    val username: String,
    val slotNumber: Int,
    val parkingLotName: String,
    val startTime: String,
    val endTime: String,
    val totalPrice: Int,
    val status: String
)