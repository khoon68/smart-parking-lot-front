package com.example.parkingapp.screens.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkingapp.components.TopBar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.parkingapp.viewmodel.ParkingListViewModel
import com.example.parkingapp.data.model.ParkingLot
import com.example.parkingapp.data.model.TimeSlot
import com.example.parkingapp.data.api.RetrofitInstance
import com.example.parkingapp.data.dto.ReservationRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext


@Composable
fun ReservationScreen(
    parking: ParkingLot,
    viewModel: ParkingListViewModel,
    navController: NavController,
    onBack: () -> Unit
) {
    val selectedSlots = remember { mutableStateListOf<TimeSlot>() } // 선택된 시간 슬롯 저장
    val reservedLabels = viewModel.getReservedTimeSlots() // 이미 예약된 시간들
    val availableTimeSlots = viewModel.getTimeSlotsForParking(parking) // ✅ 요금 반영된 슬롯 리스트

    // ✅ 선택된 시간 정렬
    val sortedSelected = selectedSlots.sortedBy { availableTimeSlots.indexOf(it) }

    // ✅ 연속된 시간인지 검사
    val isContinuous = sortedSelected.zipWithNext().all { (a, b) ->
        val aIdx = availableTimeSlots.indexOf(a)
        val bIdx = availableTimeSlots.indexOf(b)
        bIdx == aIdx + 1
    }

    val canReserve = sortedSelected.isNotEmpty() && isContinuous
    val totalPrice = selectedSlots.sumOf { it.price } // ✅ 총 요금 계산

    Scaffold(
        topBar = {
            TopBar(title = "예약 시간 선택", showBack = true, onBackClick = onBack)
        },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                Text("주차장: ${parking.name}", style = MaterialTheme.typography.titleMedium)
                Text("주소: ${parking.distance}")
                Text("요금: ${parking.pricePerHour}원/시간")
                Spacer(modifier = Modifier.height(16.dp))

                Text("예약 시간 선택", style = MaterialTheme.typography.titleMedium)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableTimeSlots) { slot ->
                        val isDisabled = slot.label() in reservedLabels
                        val isSelected = selectedSlots.contains(slot)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isDisabled -> Color.LightGray
                                        isSelected -> Color(0xFF1976D2)
                                        else -> Color(0xFFE3F2FD)
                                    }
                                )
                                .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                                .clickable(enabled = !isDisabled) {
                                    if (isSelected) selectedSlots.remove(slot)
                                    else selectedSlots.add(slot)
                                }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = slot.label(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDisabled) Color.DarkGray else Color.Black
                            )
                            Text(
                                text = "${slot.price}원",
                                fontSize = 12.sp,
                                color = if (isDisabled) Color.Gray else Color.DarkGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ 총 요금 표시
                if (selectedSlots.isNotEmpty()) {
                    Text(
                        text = "총 요금: ${totalPrice}원",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!canReserve && sortedSelected.size > 1) {
                    Text(
                        text = "연속된 시간을 선택해야 합니다.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                val context = LocalContext.current
                // 결제 화면으로 이동
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        navController.currentBackStackEntry?.savedStateHandle?.set("selectedTimeSlots", sortedSelected.map { it.label() })
                        navController.navigate("selectSlot/${parking.id}")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canReserve
                ) {
                    Text("계속하기")
                }
                /*
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val request = ReservationRequest(
                                    slotId = 1L, // ⚠️ 서버 요구사항: 슬롯 ID 필요 (임시로 1L 사용)
                                    timeSlots = sortedSelected.map { it.label() }
                                )

                                val response = RetrofitInstance.api.createReservation(request)
                                if (response.isSuccessful) {
                                    val body = response.body()

                                    // UI 작업은 Main에서 실행
                                    launch(Dispatchers.Main) {
                                        Toast.makeText(context, "예약 성공", Toast.LENGTH_SHORT).show()
                                        navController.navigate("mypage") // 또는 payment
                                    }
                                } else {
                                    launch(Dispatchers.Main) {
                                        Toast.makeText(context, "예약 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                launch(Dispatchers.Main) {
                                    Toast.makeText(context, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canReserve
                ) {
                    Text("예약하기")
                }
                */
            }
        }
    )
}

