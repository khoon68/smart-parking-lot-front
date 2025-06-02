package com.example.parkingapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reservation(
    val id: Long,
    val parkingLotName: String,
    val slotId: Long,
    val slotNumber: Int,
    val startTime: String,
    val endTime: String,
    val totalPrice: Int,
    val status: String,
    val isSlotOpened: Boolean
) : Parcelable
