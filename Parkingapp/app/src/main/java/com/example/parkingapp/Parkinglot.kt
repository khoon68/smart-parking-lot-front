package com.example.parkingapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ParkingLot(
    val id: Int,
    val name: String,
    val address: String,
    val pricePerHour: Int,
    val isAvailable: Boolean
) : Parcelable
