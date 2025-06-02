package com.example.parkingapp.repository

import com.example.parkingapp.data.api.ParkingApi
import com.example.parkingapp.data.model.ParkingLot

class ParkingLotRepository(private val api: ParkingApi) {
    suspend fun getParkingLots(): List<ParkingLot> {
        return api.getParkingLots()
    }
}
