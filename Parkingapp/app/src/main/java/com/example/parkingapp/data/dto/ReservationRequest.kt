package com.example.parkingapp.data.dto

data class ReservationRequest(
    val slotId: Long,
    val timeSlots: List<String>
)