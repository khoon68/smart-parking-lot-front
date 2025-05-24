package com.example.parkingapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reservation(
    val id: Int,
    val parking: ParkingLot,
    val timeSlots: List<String>,
    val totalPrice: Int,
    val isOngoing: Boolean = false
) : Parcelable