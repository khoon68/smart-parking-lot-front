// MainActivity.kt

package com.example.parkingapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.parkingapp.data.api.RetrofitInstance
import com.example.parkingapp.data.model.Reservation
import com.example.parkingapp.data.preferences.UserPreferences
import com.example.parkingapp.repository.ParkingLotRepository
import com.example.parkingapp.screens.auth.LoginScreen
import com.example.parkingapp.screens.auth.RegisterScreen
import com.example.parkingapp.screens.detail.ParkingDetailScreen
import com.example.parkingapp.screens.main.ParkingListScreen
import com.example.parkingapp.screens.mypage.MyPageScreen
import com.example.parkingapp.screens.parkingflow.*
import com.example.parkingapp.screens.reservation.*
import com.example.parkingapp.ui.theme.ParkingappTheme
import com.example.parkingapp.viewmodel.AuthViewModel
import com.example.parkingapp.viewmodel.ParkingListViewModel
import androidx.activity.viewModels // ✅ 이걸 꼭 import 해야 함
import com.example.parkingapp.screens.mypage.MyPageTabScreen


class MainActivity : ComponentActivity() {

    private val parkingListViewModel: ParkingListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ParkingappTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                val api = RetrofitInstance.create(context)
                val repository = ParkingLotRepository(api)

                // ViewModelFactory를 사용해 ParkingListViewModel 생성
                val viewModel: ParkingListViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ParkingListViewModel(context.applicationContext, repository) as T
                    }
                })

                // ViewModelFactory를 사용해 AuthViewModel 생성
                val authViewModel: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return AuthViewModel(context.applicationContext) as T
                    }
                })

                val lifecycleOwner  = LocalLifecycleOwner.current

                // ✅ 자동 로그아웃 처리
                DisposableEffect(Unit) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_STOP) {
                            Log.d("AutoLogout", "앱이 백그라운드로 이동됨 → 로그아웃 처리")
                            authViewModel.logout()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                // SharedPreferences에서 JWT 토큰 확인
                val userPreferences = remember { UserPreferences(context) }
                val isLoggedIn = remember { userPreferences.getToken() != null }

                // 토큰 존재 여부에 따라 시작 화면 결정
                val startDestination = if (isLoggedIn) "list" else "login"

                // 네비게이션 호스트 설정
                NavHost(navController = navController, startDestination = startDestination) {

                    // 로그인 화면
                    composable("login") {
                        LaunchedEffect(Unit) {
                            Log.d("Navigation", "📍 LoginScreen 진입")
                        }
                        LoginScreen(viewModel = authViewModel, navController = navController)
                    }

                    // 회원가입 화면
                    composable("register") {
                        LaunchedEffect(Unit) {
                            Log.d("Navigation", "📍 RegisterScreen 진입")
                        }
                        RegisterScreen(viewModel = authViewModel, navController = navController)
                    }

                    // 주차장 목록 화면
                    composable("list") {
                        LaunchedEffect(Unit) {
                            Log.d("Navigation", "📍 ParkingListScreen 진입")
                        }
                        ParkingListScreen(
                            viewModel = viewModel,
                            navController = navController
                        ) { selected ->
                            navController.navigate("detail/${selected.id}")
                        }
                    }

                    // 주차장 상세 화면
                    composable("detail/{parkingId}") { backStackEntry ->
                        LaunchedEffect(Unit) {
                            Log.d("Navigation", "📍 ParkingDetailScreen 진입")
                        }
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

                    // 예약 화면
                    composable("reservation/{parkingId}") { backStackEntry ->
                        LaunchedEffect(Unit) {
                            Log.d("Navigation", "📍 ReservationScreen 진입")
                        }
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

                    // 주차칸 선택 화면
                    composable("selectSlot/{parkingLotId}") { backStackEntry ->
                        LaunchedEffect(Unit) {
                            Log.d("Navigation", "📍 SlotSelectionScreen 진입")
                        }
                        val parkingLotId = backStackEntry.arguments?.getString("parkingLotId")?.toLongOrNull()
                        val timeSlots = navController.previousBackStackEntry?.savedStateHandle?.get<List<String>>("selectedTimeSlots")
                        val selectedDate = navController.previousBackStackEntry?.savedStateHandle?.get<String>("selectedDate")
                        if (parkingLotId != null && timeSlots != null && selectedDate != null) {
                            SlotSelectionScreen(
                                parkingLotId = parkingLotId,
                                date = selectedDate, // 테스트용 고정 날짜
                                timeSlots = timeSlots,
                                navController = navController,
                                viewModel = viewModel,
                                onSlotSelected = { selectedSlotId ->
                                    val fakeReservation = Reservation(
                                        id = viewModel.getNextReservationId(),
                                        parkingLotName = viewModel.parkingList.value.first { it.id == parkingLotId.toInt() }.name,
                                        slotId = selectedSlotId ?: 0L,
                                        slotNumber = selectedSlotId?.toInt() ?: 0,
                                        startTime = timeSlots.firstOrNull() ?: "00:00",
                                        endTime = timeSlots.lastOrNull() ?: "00:00",
                                        totalPrice = timeSlots.size * 2000,
                                        status = "RESERVED",
                                        isSlotOpened = false
                                    )
                                    navController.previousBackStackEntry?.savedStateHandle?.set("pendingReservation", fakeReservation)
                                    navController.navigate("payment")
                                }
                            )
                        }
                    }

                    // 결제 화면
                    composable("payment") {
                        LaunchedEffect(Unit) {
                            Log.d("Navigation", "📍 PaymentScreen 진입")
                        }
                        val reservation = navController.previousBackStackEntry
                            ?.savedStateHandle?.get<Reservation>("pendingReservation")
                        if (reservation != null) {
                            PaymentScreen(
                                reservation = reservation,
                                navController = navController,
                                viewModel = parkingListViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // 마이페이지 화면
                    composable("mypage") {
                        LaunchedEffect(Unit) {
                            Log.d("Navigation", "📍 MyPageTabScreen 진입")
                        }
                        MyPageTabScreen(
                            viewModel = viewModel,
                            authViewModel = authViewModel,
                            onBack = { navController.popBackStack() },
                            navController = navController
                        )
                    }

                    // 입차 안내
                    composable("notice/{reservationId}/{slotId}") { backStackEntry ->
                        LaunchedEffect(Unit) {
                            Log.d("Navigation", "📍 ParkingNoticeScreen 진입")
                        }
                        val id = backStackEntry.arguments?.getString("reservationId")?.toIntOrNull()
                        val slotId = backStackEntry.arguments?.getString("slotId")?.toLongOrNull()

                        if (id != null && slotId != null) {
                            ParkingNoticeScreen(
                                onProceed = {
                                    navController.navigate("start/$id/$slotId")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // 입차: 차단기 열기
                    composable("start/{reservationId}/{slotId}") { backStackEntry ->
                        LaunchedEffect(Unit) {
                            Log.d("Navigation", "📍 GateOpenScreen 진입")
                        }
                        val reservationId = backStackEntry.arguments?.getString("reservationId")?.toLongOrNull()
                        val slotId = backStackEntry.arguments?.getString("slotId")?.toLongOrNull()

                        if (reservationId != null && slotId != null) {
                            GateOpenScreen(
                                reservationId = reservationId,
                                slotId = slotId,
                                navController = navController,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // 출차 안내
                    composable("exitNotice/{reservationId}/{slotId}") { backStackEntry ->
                        LaunchedEffect(Unit) {
                            Log.d("Navigation", "📍 ExitNoticeScreen 진입")
                        }

                        val reservationId = backStackEntry.arguments?.getString("reservationId")?.toLongOrNull()
                        val slotId = backStackEntry.arguments?.getString("slotId")?.toLongOrNull()

                        if (reservationId != null && slotId != null) {
                            ExitNoticeScreen(
                                reservationId = reservationId,
                                slotId = slotId,
                                onBack = { navController.popBackStack() },
                                navController = navController
                            )
                        }
                    }

                    // 출차: 차단기 닫기
                    composable("gateClose/{reservationId}/{slotId}") { backStackEntry ->
                        LaunchedEffect(Unit) {
                            Log.d("Navigation", "📍 GateCloseScreen 진입")
                        }
                        val reservationId = backStackEntry.arguments?.getString("reservationId")?.toLongOrNull()
                        val slotId = backStackEntry.arguments?.getString("slotId")?.toLongOrNull()

                        if (reservationId != null && slotId != null) {
                            GateCloseScreen(
                                reservationId = reservationId,
                                slotId = slotId,
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
