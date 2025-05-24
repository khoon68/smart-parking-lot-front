package com.example.parkingapp

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.activity.viewModels
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.parkingapp.ui.theme.ParkingappTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.parkingapp.screen.ExitNoticeScreen
import com.example.parkingapp.screen.GateCloseScreen
import com.example.parkingapp.screen.GateOpenScreen
import com.example.parkingapp.screen.MyPageScreen
import com.example.parkingapp.screen.ParkingDetailScreen
import com.example.parkingapp.screen.ParkingListScreen
import com.example.parkingapp.screen.ParkingNoticeScreen
import com.example.parkingapp.screen.PaymentScreen
import com.example.parkingapp.screen.ReservationScreen


class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<ParkingListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ParkingappTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "list") {
                    composable("reservation/{parkingId}") { backStackEntry ->
                        val parkingId = backStackEntry.arguments?.getString("parkingId")?.toIntOrNull()
                        val parkingList = viewModel.parkingList.collectAsState()
                        val selected = parkingList.value.find { it.id == parkingId }

                        if (selected != null) {
                            ReservationScreen(
                                parking = selected,
                                onBack = { navController.popBackStack() },
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                    }
                    composable("mypage") {
                        MyPageScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            navController = navController
                        )
                    }
                    composable("list") {
                        ParkingListScreen(
                            viewModel = viewModel,
                            navController = navController
                        ) { selected ->
                            navController.navigate("detail/${selected.id}")  // ✅ 여기서 이동함
                        }
                    }
                    composable("detail/{parkingId}") { backStackEntry ->   // ✅ detail 경로 추가!
                        val parkingId = backStackEntry.arguments?.getString("parkingId")?.toIntOrNull()
                        val parkingList by viewModel.parkingList.collectAsState()
                        val selected = remember(parkingList, parkingId) {
                            parkingList.find { it.id == parkingId }
                        }
                        if (selected != null) {
                            ParkingDetailScreen(
                                parking = selected,
                                onReserveClick = { navController.navigate("reservation/$it") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    composable("payment") {
                        val reservation = navController.previousBackStackEntry
                            ?.savedStateHandle?.get<Reservation>("pendingReservation")
                        if (reservation != null) {
                            PaymentScreen(
                                reservation = reservation,
                                onConfirm = {
                                    viewModel.addReservation(reservation)
                                    navController.navigate("mypage") {
                                        popUpTo("list") { inclusive = false }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    // 일회용 키 발급 화면
                    composable("start/{reservationId}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("reservationId")?.toIntOrNull()
                        if (id != null) {
                            GateOpenScreen(
                                reservationId = id,
                                navController = navController,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    // 주차 시 주의사항
                    composable("notice/{reservationId}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("reservationId")?.toIntOrNull()
                        if (id != null) {
                            ParkingNoticeScreen(
                                onProceed = {
                                    navController.navigate("start/$id")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    // 출차 시 주의사항
                    composable("exitNotice/{reservationId}") { backStackEntry ->
                        val reservationId = backStackEntry.arguments?.getString("reservationId")?.toIntOrNull()
                        if (reservationId != null) {
                            ExitNoticeScreen(
                                onProceed = {
                                    navController.navigate("gateClose/$id")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    // 출차
                    composable("gateClose/{reservationId}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("reservationId")?.toIntOrNull()
                        if (id != null) {
                            GateCloseScreen(
                                reservationId = id,
                                navController = navController,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    // 차단기 제어 화면
                    composable("start/{reservationId}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("reservationId")?.toIntOrNull()
                        if (id != null) {
                            GateOpenScreen(
                                reservationId = id,
                                navController = navController,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}




