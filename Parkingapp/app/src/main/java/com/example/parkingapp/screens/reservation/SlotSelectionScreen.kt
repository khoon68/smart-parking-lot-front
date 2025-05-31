package com.example.parkingapp.screens.reservation

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
import androidx.compose.ui.unit.Dp
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

    val testSlots = listOf(
        ParkingSlotDTO(id = 1, slotNumber = 1, available = true, opened = false),
        ParkingSlotDTO(id = 2, slotNumber = 2, available = false, opened = false),
        ParkingSlotDTO(id = 3, slotNumber = 3, available = true, opened = true)
    )

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // val response = RetrofitInstance.create(context).getAvailableSlots(parkingLotId, date, timeSlots)
                slots = testSlots
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
                            onClick = { selectedSlotId = slot.id },
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
                                .height(180.dp) // 직사각형 형태

                        ) {
                            Text("슬롯 ${slot.slotNumber}")
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (selectedSlotId == null) {
                            Toast.makeText(context, "슬롯을 선택하세요", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        scope.launch {
                            try {
                                val response = RetrofitInstance.create(context).createReservation(
                                    ReservationRequest(
                                        slotId = selectedSlotId!!,
                                        timeSlots = timeSlots
                                    )
                                )
                                if (response.isSuccessful) {
                                    val body = response.body()
                                    val parking = viewModel.parkingList.value.firstOrNull { it.id == parkingLotId.toInt() }
                                    if (body != null && parking != null) {
                                        val reservation = Reservation(
                                            id = body.reservationId,
                                            parking = parking,
                                            timeSlots = timeSlots,
                                            totalPrice = body.totalPrice,
                                            isOngoing = false,
                                            slotId = selectedSlotId!!
                                        )
                                        navController.currentBackStackEntry?.savedStateHandle?.set("pendingReservation", reservation)
                                        navController.navigate("payment")
                                    }
                                } else {
                                    Toast.makeText(context, "예약 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
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
