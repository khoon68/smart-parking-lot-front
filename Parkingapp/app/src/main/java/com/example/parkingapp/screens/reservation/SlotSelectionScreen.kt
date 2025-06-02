package com.example.parkingapp.screens.reservation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import com.example.parkingapp.components.TopBar
import com.example.parkingapp.data.api.RetrofitInstance
import com.example.parkingapp.data.dto.ParkingSlotDTO
import com.example.parkingapp.data.dto.ReservationRequest
import com.example.parkingapp.data.model.Reservation
import com.example.parkingapp.viewmodel.ParkingListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotSelectionScreen(
    parkingLotId: Long,
    date: String,
    timeSlots: List<String>,
    navController: NavController,
    viewModel: ParkingListViewModel,
    onSlotSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var slots by remember { mutableStateOf<List<ParkingSlotDTO>>(emptyList()) }
    var selectedSlotId by remember { mutableStateOf<Long?>(null) }
    var selectedSlotNumber by remember { mutableStateOf<Int?>(null) }

    // ✅ 슬롯 목록 불러오기
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitInstance.create(context)
                    .getAvailableSlots(parkingLotId, date, timeSlots)
                slots = response
            } catch (e: Exception) {
                Toast.makeText(context, "슬롯 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(title = "슬롯 선택", showBack = true, onBackClick = { navController.popBackStack() })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            if (slots.isEmpty()) {
                Text("예약 가능한 슬롯이 없습니다.")
            } else {
                Text("예약 가능한 슬롯 목록", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxHeight(0.6f)
                ) {
                    items(slots) { slot ->
                        val isSelected = slot.id == selectedSlotId
                        Button(
                            onClick = {
                                selectedSlotId = slot.id
                                selectedSlotNumber = slot.slotNumber
                            },
                            enabled = slot.available,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    !slot.available -> MaterialTheme.colorScheme.surfaceVariant
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.secondaryContainer
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            Text("슬롯 ${slot.slotNumber}${if (isSelected) " ✅" else ""}")
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (selectedSlotId == null || selectedSlotNumber == null) {
                            Toast.makeText(context, "슬롯을 선택하세요", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        scope.launch {
                            try {
                                val request = ReservationRequest(
                                    slotId = selectedSlotId!!,
                                    timeSlots = timeSlots
                                )
                                val response = RetrofitInstance.create(context)
                                    .createReservation(request)

                                if (response.isSuccessful) {
                                    val body = response.body()
                                    val sorted = timeSlots.sorted()
                                    val startTime = sorted.first()
                                    val endTime = sorted.lastOrNull()?.let {
                                        val hour = it.substringBefore(":").toIntOrNull() ?: 0
                                        String.format("%02d:00", (hour + 1) % 24)
                                    } ?: startTime

                                    val parking = viewModel.parkingList.value.firstOrNull {
                                        it.id == parkingLotId.toInt()
                                    }
                                    if (body != null && parking != null) {
                                        val reservation = Reservation(
                                            id = body.reservationId,
                                            parkingLotName = parking.name,
                                            slotId = body.slotId,
                                            slotNumber = selectedSlotNumber!!,
                                            startTime = startTime,
                                            endTime = endTime,
                                            totalPrice = body.totalPrice,
                                            status = body.status,
                                            isSlotOpened = body.status == "ACTIVE"
                                        )

                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "pendingReservation", reservation
                                        )
                                        navController.navigate("payment")
                                    }
                                } else {
                                    Toast.makeText(context, "예약 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = selectedSlotId != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("예약하기")
                }
            }
        }
    }
}