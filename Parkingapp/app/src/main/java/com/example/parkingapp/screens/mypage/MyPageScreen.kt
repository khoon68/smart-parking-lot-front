package com.example.parkingapp.screens.mypage

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkingapp.components.ReservationStatusControl
import com.example.parkingapp.components.TopBar
import com.example.parkingapp.viewmodel.ParkingListViewModel
import com.example.parkingapp.viewmodel.AuthViewModel

@Composable
fun MyPageScreen(
    viewModel: ParkingListViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    navController: NavController
) {


    val context = LocalContext.current
    val reservations by viewModel.reservationHistory.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedReservationId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(navController.currentBackStackEntry) {
        viewModel.fetchMyReservations()
    }
    LaunchedEffect(reservations) {
        Log.d("MyPage", "🔥 예약 목록 변경됨: ${reservations.map { it.status to it.isSlotOpened }}")
    }

    Scaffold(
        topBar = { TopBar(title = "예약 내역", showBack = true, onBackClick = onBack) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Button(
                onClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("list") { inclusive = true }
                    }
                    Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Text("로그아웃")
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(reservations) { reservation ->

                    val statusLabel = when (reservation.status) {
                        "RESERVED" -> "대기"
                        "ACTIVE" -> "활성"
                        "CANCELLED" -> "취소"
                        "COMPLETED" -> "완료"
                        else -> "알 수 없음"
                    }
                    val statusColor = when (reservation.status) {
                        "RESERVED" -> Color(0xFF1976D2) // 진한 파랑
                        "ACTIVE" -> Color(0xFF388E3C)   // 진한 초록
                        "COMPLETED" -> Color(0xFF616161) // 진한 회색
                        "CANCELLED" -> Color(0xFFD32F2F) // 진한 빨강
                        else -> Color.Gray
                    }
                    val canCancel = reservation.status == "RESERVED"

                    val isActive = reservation.status == "ACTIVE"
                    val isSlotOpened = reservation.isSlotOpened
                    Log.d("MyPageScreen", "🚧 Reservation ${reservation.id}: isSlotOpened = $isSlotOpened")


                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("주차장: ${reservation.parkingLotName}", style = MaterialTheme.typography.titleMedium)
                            Text("슬롯 번호: ${reservation.slotNumber}", style = MaterialTheme.typography.bodyMedium)
                            Text("시간: ${reservation.startTime} ~ ${reservation.endTime}", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "상태: $statusLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = statusColor
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.cancelReservationFromServer(
                                            reservationId = reservation.id,
                                            onSuccess = {
                                                Toast.makeText(context, "예약이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                                            },
                                            onFailure = { msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    },
                                    enabled = canCancel
                                ) {
                                    Text("예약 취소")
                                }

                                if (isActive) {

                                    if (isSlotOpened) {
                                        Button(onClick = { navController.navigate("exitNotice/${reservation.id}/${reservation.slotId}") }) {
                                            Text("주차 종료")
                                        }
                                    } else {
                                        Button(onClick = { navController.navigate("notice/${reservation.id}/${reservation.slotId}") }) {
                                            Text("주차 시작")
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                    ) {
                                        Text("")
                                    }
                                }
                            }

                            ReservationStatusControl(
                                reservationId = reservation.id.toLong(),
                                viewModel = viewModel  // ParkingListViewModel
                            )
                        }
                    }
                }
            }
        }

        if (showDialog && selectedReservationId != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("예약 취소") },
                text = { Text("이 예약을 정말 취소하시겠습니까?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.cancelReservation(selectedReservationId!!)
                        showDialog = false
                        selectedReservationId = null
                    }) {
                        Text("확인")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        selectedReservationId = null
                    }) {
                        Text("취소")
                    }
                }
            )
        }
    }
}
