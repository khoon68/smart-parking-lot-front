package com.example.parkingapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reservation(
    val id: Int,
    val parking: ParkingLot,
    val timeSlots: List<String>,
    val totalPrice: Int,
    val isOngoing: Boolean = false,
    val slotId: Long
) : Parcelable

