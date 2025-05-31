package com.example.parkingapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingapp.data.api.RetrofitInstance
import com.example.parkingapp.data.dto.BarrierOpenRequest
import com.example.parkingapp.data.model.ParkingLot
import com.example.parkingapp.data.model.Reservation
import com.example.parkingapp.data.model.TimeSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ParkingListViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    private val api = RetrofitInstance.create(context)

    private val _parkingList = MutableStateFlow<List<ParkingLot>>(emptyList())
    val parkingList: StateFlow<List<ParkingLot>> = _parkingList

    private val _reservationHistory = MutableStateFlow<List<Reservation>>(emptyList())  // ✅ 선언 다시 추가
    val reservationHistory: StateFlow<List<Reservation>> = _reservationHistory

    init {
        fetchParkingLots()

        // ✅ 테스트용 주차장 더미 데이터
        _parkingList.value = listOf(
            ParkingLot(
                id = 1,
                name = "테스트 주차장 A",
                distance = 150,
                pricePerHour = 2000,
                availableSlots = 4,
                isAvailable = true
            ),
            ParkingLot(
                id = 2,
                name = "테스트 주차장 B",
                distance = 300,
                pricePerHour = 2500,
                availableSlots = 2,
                isAvailable = false
            )
        )
        // ✅ 서버 없이 테스트 예약 데이터
        _reservationHistory.value = listOf(
            Reservation(
                id = 999,
                parking = ParkingLot(
                    id = 1,
                    name = "테스트 주차장",
                    distance = 100,
                    pricePerHour = 2000,
                    availableSlots = 3,
                    isAvailable = true
                ),
                timeSlots = listOf("09:00", "10:00", "11:00"),
                totalPrice = 6000,
                isOngoing = false,
                slotId = 1L
            )
        )
    }
    fun cancelReservationFromServer(
        reservationId: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.create(appContext).cancelReservation(reservationId.toLong())
                if (response.isSuccessful) {
                    cancelReservation(reservationId)  // ✅ 내부 리스트에서 제거
                    onSuccess()
                } else {
                    onFailure("서버 오류: ${response.code()}")
                }
            } catch (e: Exception) {
                onFailure("네트워크 오류: ${e.message}")
            }
        }
    }

    fun fetchMyReservations() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.create(appContext).getMyReservations()
                val parsed = response.map {
                    Reservation(
                        id = it.reservationId,
                        parking = ParkingLot(  // 서버 응답에 위치/요금/이미지 없음 → 일부 더미값 사용
                            id = 0,
                            name = it.parkingLotName,
                            distance = 0,
                            pricePerHour = 0,
                            availableSlots = 0
                        ),
                        timeSlots = listOf("${it.startTime}~${it.endTime}"),
                        totalPrice = it.totalPrice,
                        isOngoing = it.status == "ONGOING",
                        slotId = 1L
                    )
                }
                _reservationHistory.value = parsed
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchParkingLots() {
        viewModelScope.launch {
            try {
                val result = api.getParkingLots()
                _parkingList.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ✅ 새 예약 추가 함수
    fun addReservation(reservation: Reservation) {
        _reservationHistory.value = _reservationHistory.value + reservation
    }
    // 예약
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

    // ✅ 주차 시작 처리 (실제 주차장 입차 시 호출)
    fun markReservationStarted(reservationId: Int) {
        _reservationHistory.value = _reservationHistory.value.map {
            if (it.id == reservationId) it.copy(isOngoing = true) else it
        }
    }
    // 차단기 오픈
    fun openBarrier(
        reservationId: Int,
        slotId: Long,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.create(appContext).openBarrier(
                    BarrierOpenRequest(slotId))
                if (response.isSuccessful) {
                    markReservationStarted(reservationId)
                    onSuccess()
                } else {
                    onFailure("입차 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                onFailure("네트워크 오류: ${e.message}")
            }
        }
    }
    // 차단기 클로즈
    fun closeBarrier(
        reservationId: Int,
        slotId: Long,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.create(appContext).closeBarrier(BarrierOpenRequest(slotId))
                if (response.isSuccessful) {
                    markReservationEnded(reservationId)
                    cancelReservation(reservationId)
                    onSuccess()
                } else {
                    onFailure("출차 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                onFailure("네트워크 오류: ${e.message}")
            }
        }
    }

    // ✅ 주차 종료 처리
    fun markReservationEnded(reservationId: Int) {
        _reservationHistory.value = _reservationHistory.value.map {
            if (it.id == reservationId) it.copy(isOngoing = false) else it
        }
    }
}
