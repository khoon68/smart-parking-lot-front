package com.example.parkingapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ParkingLot(
    val id: Int,
    val name: String,
    val distance: Int,
    val pricePerHour: Int,
    val availableSlots: Int,
    val isAvailable: Boolean,
    val imageUrl: String? = null
) : Parcelable
