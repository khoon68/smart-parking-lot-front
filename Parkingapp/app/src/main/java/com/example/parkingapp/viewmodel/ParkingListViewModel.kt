package com.example.parkingapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingapp.data.api.RetrofitInstance
import com.example.parkingapp.data.dto.BarrierOpenRequest
import com.example.parkingapp.data.model.ParkingLot
import com.example.parkingapp.data.model.Reservation
import com.example.parkingapp.data.model.TimeSlot
import com.example.parkingapp.repository.ParkingLotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ParkingListViewModel(context: Context, private val repository: ParkingLotRepository) : ViewModel() {

    private val appContext = context.applicationContext
    private val api = RetrofitInstance.create(context)

    private val _parkingList = MutableStateFlow<List<ParkingLot>>(emptyList())
    val parkingList: StateFlow<List<ParkingLot>> = _parkingList

    private val _reservationHistory = MutableStateFlow<List<Reservation>>(emptyList())
    val reservationHistory: StateFlow<List<Reservation>> = _reservationHistory

    init {
        fetchParkingLots()
        Log.d("fetched", "주차장 목록 불러옴")
    }

    /** 주차장 목록 새로 불러오기 */
    fun loadParkingLots() {
        viewModelScope.launch {
            _parkingList.value = repository.getParkingLots()
        }
    }

    /** 서버에 예약 취소 요청 후 로컬 상태에서도 제거 */
    fun cancelReservationFromServer(
        reservationId: Long,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = api.cancelReservation(reservationId.toLong())
                if (response.isSuccessful) {
                    _reservationHistory.value = _reservationHistory.value.map {
                        if (it.id == reservationId) it.copy(status = "CANCELLED") else it
                    }
                    onSuccess()
                } else {
                    onFailure("서버 오류: \${response.code()}")
                }
            } catch (e: Exception) {
                onFailure("네트워크 오류: \${e.message}")
            }
        }
    }

    /** 내 예약 목록을 서버에서 조회 */
    fun fetchMyReservations() {
        viewModelScope.launch {
            try {
                val response = api.getMyReservations()
                val parsed = response.map {
                    Reservation(
                        id = it.reservationId,
                        parkingLotName = it.parkingLotName,
                        slotId = it.slotId,
                        slotNumber = it.slotNumber,
                        startTime = it.startTime,
                        endTime = it.endTime,
                        totalPrice = it.totalPrice,
                        status = it.status,
                        isSlotOpened = it.isSlotOpened
                    )
                }
                _reservationHistory.value = parsed
            } catch (e: Exception) {
                Log.e("fetchMyReservations", "에러: ${e.message}")
            }
        }
    }

    /** 주차장 목록 조회 */
    fun fetchParkingLots() {
        viewModelScope.launch {
            try {
                val result = api.getParkingLots().map {
                    val available = it.availableSlots > 0
                    Log.d("fetched", "🚗 \${it.name}: slots=\${it.availableSlots}, isAvailable=\$available")
                    it.copy(
                        isAvailable = available,
                        distance = it.distance
                    )
                }
                _parkingList.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** 다음 예약 ID 계산 */
    fun getNextReservationId(): Long {
        return (_reservationHistory.value.maxOfOrNull { it.id } ?: 0) + 1
    }

    /** 예약 ID에 해당하는 예약을 로컬 상태에서 제거 */
    fun cancelReservation(reservationId: Long) {
        _reservationHistory.value = _reservationHistory.value.filterNot {
            it.id == reservationId
        }
    }

    /** 특정 주차장에 대해 전체 시간 슬롯 반환 */
    fun getTimeSlotsForParking(parking: ParkingLot): List<TimeSlot> {
        return (0 until 24).map { hour ->
            val start = String.format("%02d:00", hour)
            val end = String.format("%02d:00", (hour + 1) % 24)
            TimeSlot(startTime = start, endTime = end).copyWithPrice(parking.pricePerHour)
        }
    }

    /** 예약된 시간 슬롯 문자열 집합 반환 */
    fun getReservedTimeSlots(): Set<String> {
        return reservationHistory.value.map { "\${it.startTime}~\${it.endTime}" }.toSet()
    }

    /** 예약 시작 상태 표시 */
    fun markReservationStarted(reservationId: Long) {
        _reservationHistory.value = _reservationHistory.value.map {
            if (it.id == reservationId) it.copy(isSlotOpened = true) else it
        }
    }

    /** 차단기 열기 요청 */
    fun openBarrier(
        reservationId: Long,
        slotId: Long,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = api.openBarrier(slotId) // 요청 방식 변경
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

    /** 차단기 닫기 요청 → 예약 종료 처리 */
    fun closeBarrier(
        reservationId: Long,
        slotId: Long,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = api.closeBarrier(slotId)
                if (response.isSuccessful) {
                    updateReservationStatusLocally(reservationId, "COMPLETED") // 상태 변경
                    markReservationEnded(reservationId) // 차단기 닫힘 표시
                    onSuccess()
                } else {
                    onFailure("출차 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                onFailure("네트워크 오류: ${e.message}")
            }
        }
    }

    /** 예약 종료 상태 표시 */
    fun markReservationEnded(reservationId: Long) {
        _reservationHistory.value = _reservationHistory.value.map {
            if (it.id == reservationId) it.copy(isSlotOpened = false) else it
        }
    }

    /** 예약 상태 변경 (프론트 반영용) */
    fun updateReservationStatusLocally(reservationId: Long, newStatus: String) {
        _reservationHistory.value = _reservationHistory.value.map {
            if (it.id == reservationId) it.copy(status = newStatus) else it
        }
    }

    /** 서버에서 예약을 다시 불러와 덮어쓰기 하므로 addReservation은 더 이상 사용 안함 */
    // fun addReservation(reservation: Reservation) {
    //     _reservationHistory.value = _reservationHistory.value + reservation
    // }
}
