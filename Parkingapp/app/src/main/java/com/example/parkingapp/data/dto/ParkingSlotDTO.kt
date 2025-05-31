package com.example.parkingapp.data.dto

data class ParkingSlotDTO(
    val id: Long,
    val slotNumber: Int,
    val available: Boolean,
    val opened: Boolean
)
