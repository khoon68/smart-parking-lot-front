package com.example.parkingapp.screens.reservation

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkingapp.components.TopBar
import com.example.parkingapp.data.api.RetrofitInstance
import com.example.parkingapp.data.dto.ReservationRequest
import com.example.parkingapp.data.model.Reservation
import com.example.parkingapp.ui.theme.formatContinuousTime
import com.example.parkingapp.viewmodel.ParkingListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PaymentScreen(
    reservation: Reservation,
    navController: NavController,
    viewModel: ParkingListViewModel,
    onBack: () -> Unit // ← 이건 더 이상 사용하지 않음
) {
    val context = LocalContext.current

    // ✅ 시스템 뒤로가기 버튼 막기
    BackHandler(enabled = true) {
        // 아무 작업도 하지 않음 → 뒤로가기 비활성화
    }

    Scaffold(
        topBar = {
            // ✅ TopBar의 뒤로가기 버튼 제거
            TopBar(title = "결제", showBack = false, onBackClick = {})
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("주차장: ${reservation.parkingLotName}", style = MaterialTheme.typography.titleMedium)
            Text("예약 시간: ${reservation.startTime} ~ ${reservation.endTime}")
            Text("총 요금: ${reservation.totalPrice}원")

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    Toast.makeText(context, "마이페이지로 이동합니다.", Toast.LENGTH_SHORT).show()
                    navController.navigate("mypage") {
                        popUpTo("list") { inclusive = false }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("마이페이지로 이동")
            }
        }
    }
}