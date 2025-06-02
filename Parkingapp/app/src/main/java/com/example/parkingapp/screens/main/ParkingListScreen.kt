// 📄 파일 경로: com/example/parkingapp/screens/main/ParkingListScreen.kt

package com.example.parkingapp.screens.main

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.parkingapp.screens.detail.ParkingItem
import com.example.parkingapp.viewmodel.ParkingListViewModel
import com.example.parkingapp.data.model.ParkingLot

@Composable
fun ParkingListScreen(
    viewModel: ParkingListViewModel,
    navController: NavController,
    onItemClick: (ParkingLot) -> Unit
) {
    // ✅ 로그인 후 진입 시 서버에서 주차장 목록 다시 불러오기
    LaunchedEffect(Unit) {
        viewModel.fetchParkingLots()
    }

    val parkingList by viewModel.parkingList.collectAsState()

    // ✅ 디버깅용 로그: 실제 표시 여부 확인
    LaunchedEffect(parkingList) {
        Log.d("UI", "🟢 UI 수신 데이터:")
        parkingList.forEach {
            Log.d("UI", "🟢 ${it.name} isAvailable = ${it.isAvailable}, availableSlots = ${it.availableSlots}")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(parkingList) { parking ->
                ParkingItem(parking = parking) {
                    onItemClick(parking)
                }
            }
        }

        Button(
            onClick = { navController.navigate("mypage") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("마이페이지로 가기")
        }
    }
}
