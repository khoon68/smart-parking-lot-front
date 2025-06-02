package com.example.parkingapp.data.dto

import com.google.gson.annotations.SerializedName

data class ReservationResponse(
    val reservationId: Long,
    val username: String,
    val slotId: Long,
    val slotNumber: Int,
    val parkingLotName: String,
    val startTime: String,
    val endTime: String,
    val totalPrice: Int,
    val status: String,
    @SerializedName("slotOpened")
    val isSlotOpened: Boolean
)