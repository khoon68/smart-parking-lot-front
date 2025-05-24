package com.example.parkingapp

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ParkingListViewModel : ViewModel() {
    // 예시 주차장 리스트
    private val _parkingList = MutableStateFlow(
        listOf(
            ParkingLot(1, "공대 1호관 주차장 1", "49m", 2000, false),
            ParkingLot(2, "공대 1호관 주차장 2", "50m", 2000, true),
            ParkingLot(3, "공대 2호관 주차장 1", "117m", 1800, false),
            ParkingLot(4, "공대 2호관 주차장 2", "129m", 1800, true),
            ParkingLot(5, "어울림관 주차장", "162m", 1500, false),
            ParkingLot(6, "기술대학관 주차장", "251m", 2200, true),
        )
    )
    val parkingList: StateFlow<List<ParkingLot>> = _parkingList // 외부에서 읽기 전용으로 제공

    // ✅ 예약 내역 관리
    private val _reservationHistory = MutableStateFlow<List<Reservation>>(emptyList())
    val reservationHistory: StateFlow<List<Reservation>> = _reservationHistory // 예약 내역

    // ✅ 새 예약 추가 함수
    fun addReservation(reservation: Reservation) {
        _reservationHistory.value = _reservationHistory.value + reservation
    }
    // ✅ 다음 예약 ID 생성 함수
    fun getNextReservationId(): Int {
        return (_reservationHistory.value.maxOfOrNull { it.id } ?: 0) + 1
    }
    // ✅ 예약 취소 함수
    fun cancelReservation(reservationId: Int) {
        _reservationHistory.value = _reservationHistory.value.filterNot {
            it.id == reservationId || it.isOngoing
        }
    }
    // 전체 사용 가능한 시간 슬롯
    fun getTimeSlotsForParking(parking: ParkingLot): List<TimeSlot> {
        return listOf(
            TimeSlot("08:00", "09:00"),
            TimeSlot("09:00", "10:00"),
            TimeSlot("10:00", "11:00"),
            TimeSlot("11:00", "12:00"),
            TimeSlot("12:00", "13:00"),
            TimeSlot("13:00", "14:00")
        ).map { it.copyWithPrice(parking.pricePerHour) }
    }

    // 이미 예약된 시간 슬롯 (문자열 기준)
    fun getReservedTimeSlots(): Set<String> {
        return reservationHistory.value.flatMap { it.timeSlots }.toSet()
    }

    // ✅ 예약시간 겹침 검사 (문자열 기반)
    fun hasTimeConflict(newSlots: List<String>): Boolean {
        val allReservedSlots = reservationHistory.value.flatMap { it.timeSlots }
        return newSlots.any { it in allReservedSlots }
    }

    // ✅ 주차 시작 처리 (실제 주차장 입차 시 호출)
    fun markReservationStarted(reservationId: Int) {
        _reservationHistory.value = _reservationHistory.value.map {
            if (it.id == reservationId) it.copy(isOngoing = true) else it
        }
    }

    // ✅ 주차 종료 처리
    fun markReservationEnded(reservationId: Int) {
        _reservationHistory.value = _reservationHistory.value.map {
            if (it.id == reservationId) it.copy(isOngoing = false) else it
        }
    }
}
