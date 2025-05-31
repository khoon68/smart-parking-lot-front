package com.example.parkingapp.data.model

data class TimeSlot(
    val startTime: String,     // "09:00"
    val endTime: String,       // "10:00"
    val price: Int = 3000
) {
    fun label(): String = "$startTime~$endTime"

    // ✅ 주차장 가격 기반으로 새로운 TimeSlot 복사 생성
    fun copyWithPrice(newPrice: Int): TimeSlot {
        return this.copy(price = newPrice)
    }
}
