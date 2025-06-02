package com.example.parkingapp.screens.mypage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkingapp.viewmodel.AuthViewModel
import com.example.parkingapp.viewmodel.ParkingListViewModel
import com.example.parkingapp.components.TopBar
import com.example.parkingapp.data.model.Reservation
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.parkingapp.components.ReservationStatusControl

@Composable
fun MyPageTabScreen(
    viewModel: ParkingListViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val reservations by viewModel.reservationHistory.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("전체", "대기", "활성", "완료", "취소됨")

    val filtered = when (selectedTab) {
        1 -> reservations.filter { it.status == "RESERVED" }
        2 -> reservations.filter { it.status == "ACTIVE" }
        3 -> reservations.filter { it.status == "COMPLETED" }
        4 -> reservations.filter { it.status == "CANCELLED" }
        else -> reservations
    }

    LaunchedEffect(Unit) {
        viewModel.fetchMyReservations()
    }

    var showDialog by remember { mutableStateOf(false) }
    var selectedReservationId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = { TopBar(title = "예약 내역", showBack = true, onBackClick = onBack) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
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

            TabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filtered) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        viewModel = viewModel,
                        navController = navController,
                        authViewModel = authViewModel,
                        context = context,
                        onCancelConfirm = {
                            selectedReservationId = reservation.id
                            showDialog = true
                        }
                    )
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
                        viewModel.cancelReservationFromServer(
                            reservationId = selectedReservationId!!,
                            onSuccess = {
                                Toast.makeText(context, "예약이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                                viewModel.fetchMyReservations()
                            },
                            onFailure = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
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

@Composable
fun ReservationCard(
    reservation: Reservation,
    viewModel: ParkingListViewModel,
    navController: NavController,
    authViewModel: AuthViewModel,
    context: android.content.Context,
    onCancelConfirm: () -> Unit
) {
    val statusLabel = when (reservation.status) {
        "RESERVED" -> "대기"
        "ACTIVE" -> "활성"
        "CANCELLED" -> "취소"
        "COMPLETED" -> "완료"
        else -> "알 수 없음"
    }
    val statusColor = when (reservation.status) {
        "RESERVED" -> Color(0xFF1976D2)
        "ACTIVE" -> Color(0xFF388E3C)
        "COMPLETED" -> Color(0xFF616161)
        "CANCELLED" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }
    val canCancel = reservation.status == "RESERVED"
    val isActive = reservation.status == "ACTIVE"
    val isSlotOpened = reservation.isSlotOpened

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("예약 인덱스: ${reservation.id}", style = MaterialTheme.typography.titleMedium)
            Text("주차장: ${reservation.parkingLotName}", style = MaterialTheme.typography.titleMedium)
            Text("슬롯 번호: ${reservation.slotNumber}", style = MaterialTheme.typography.bodyMedium)
            Text("시간: ${reservation.startTime} ~ ${reservation.endTime}", style = MaterialTheme.typography.bodyMedium)
            Text("상태: $statusLabel", style = MaterialTheme.typography.bodyMedium, color = statusColor)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
                if (reservation.status == "RESERVED") {
                    Button(
                        onClick = onCancelConfirm,
                        enabled = canCancel
                    ) {
                        Text("예약 취소")
                    }
                }
                if (isActive) {
                    val path = if (isSlotOpened) "exitNotice" else "notice"
                    Button(onClick = { navController.navigate("$path/${reservation.id}/${reservation.slotId}") }) {
                        Text(if (isSlotOpened) "주차 종료" else "주차 시작")
                    }
                }
//                else {
//                    Button(
//                        onClick = {},
//                        enabled = false,
//                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
//                    ) {
//                        Text("")
//                    }
//                }
            }
            if (reservation.status == "RESERVED") {
                ReservationStatusControl(
                    reservationId = reservation.id.toLong(),
                    viewModel = viewModel  // ParkingListViewModel
                )
            }
        }
    }
}
