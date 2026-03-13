package com.example.gymtracking

import android.net.Uri
import android.os.Bundle
import android.os.Build
import androidx.compose.foundation.shape.CircleShape
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import java.util.concurrent.TimeUnit
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

import kotlinx.coroutines.delay




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: Any) {
    object Home : Screen("home", "Home", R.drawable.ic_home)
    object Programs : Screen("programs", "Programlar", R.drawable.ic_programs)
    object Workout : Screen("workout", "Antrenman", R.drawable.ic_workout)
    object Progress : Screen("progress", "Gelişim", R.drawable.ic_progress)
    object CreateProgram : Screen("create_program", "Yeni Program", Icons.Default.Add)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // İzin verildiğinde yapılacak işlemler (opsiyonel)
            Toast.makeText(context, "Bildirim izni verildi.", Toast.LENGTH_SHORT).show()
        }
    }
    val database = remember { AppDatabase.getDatabase(context) }
    val workoutDao = database.workoutDao()

    val programs by workoutDao.getAllPrograms().collectAsState(initial = emptyList())
    val pr by workoutDao.getAllRecords().collectAsState(initial = emptyList())
    val bodyMeasurements by workoutDao.getAllMeasurements().collectAsState(initial = emptyList())
    val photos by workoutDao.getAllPhotos().collectAsState(initial = emptyList())
    
    var isLoading by remember { mutableStateOf(true) }
    var editingProgram by remember { mutableStateOf<WorkoutProgram?>(null) }


    LaunchedEffect(programs) {
        if (programs.isNotEmpty()) {
            WorkoutPrefs.checkAndShiftDay(context, programs.size)
        }
    }

    LaunchedEffect(Unit) {
            // el ile tarih ayarla
        /*
        val prefs = context.getSharedPreferences("workout_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("total_days_count", 28)     // Günü 28 yapar
            .putInt("current_program_index", 6) // İlk programı seçer (1/7 için)
            .apply()


         */

        // --- ADIM 2: ANDROID 13+ İÇİN İZİN İSTE ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        delay(2000)
        isLoading = false

        // --- ADIM 3: WORKMANAGER ZAMANLAMASI ---
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            24, TimeUnit.HOURS
        ).setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WorkoutReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    // Gecikmeyi hesaplayan yardımcı fonksiyon (Akşam 20:00 için)
    fun calculateInitialDelay(): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 1) // Saat 20:00
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return calendar.timeInMillis - now
    }

    val userProfile = if (bodyMeasurements.isNotEmpty()) {
        val last = bodyMeasurements.last()
        UserProfile(last.weight, last.bodyFat, 1)
    } else null

    if (isLoading) {
        SplashScreen()
    } else {
        Scaffold(
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                if (currentRoute != Screen.CreateProgram.route) {
                    BottomNavigationBar(navController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {

                composable("macros_screen") {
                    MacrosScreen(
                        onSave = { macros: UserMacros ->
                            scope.launch {
                                workoutDao.insertMacros(macros) // Veritabanına kaydet
                                navController.popBackStack() // Geri dön
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Home.route) {
                    val nextIdx = WorkoutPrefs.getNextProgramIndex(context, programs.size)
                    val activeProgram = if (programs.isNotEmpty()) programs.getOrNull(nextIdx) else null
                    val isFinishedToday = WorkoutPrefs.isWorkoutFinishedToday(context)

                    HomeScreen(
                        totalDays = WorkoutPrefs.getTotalDaysCount(context),
                        activeProgram = activeProgram,
                        isFinishedToday = isFinishedToday,
                        programs = programs,
                        navController = navController,
                        userProfile = userProfile,
                        photos = photos,
                        lastRecord = pr.lastOrNull(),
                        onProfileSave = { profile ->
                            scope.launch {
                                workoutDao.insertMeasurement(BodyMeasurement(weight = profile.weight, bodyFat = profile.bodyFat, date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())))
                            }
                        }
                    )
                }

                // NavHost içindeki Programs rotası
                composable(Screen.Programs.route) {
                    ProgramsScreen(
                        programs = programs,
                        onAddNew = {
                            editingProgram = null // Yeni eklerken içi boş olsun
                            navController.navigate(Screen.CreateProgram.route)
                        },
                        onDelete = { program -> scope.launch { workoutDao.deleteProgram(program) } },
                        onBack = { navController.popBackStack() },

                        // İŞTE BURASI: Düzenle butonuna basınca ne olacak?
                        onEdit = { program ->
                            editingProgram = program // MainActivity'de tanımladığın 'editingProgram' değişkenine ata
                            navController.navigate(Screen.CreateProgram.route) // Oluşturma sayfasına git
                        }
                    )
                }

                composable(Screen.Progress.route) {
                    val lastMacros by workoutDao.getLastMacros().collectAsState(initial = null)
                    ProgressScreen(
                        contentAfterPhotos={MacroSummaryCard(macros = lastMacros)},
                        records = pr,
                        measurements = bodyMeasurements,
                        userProfile = userProfile,
                        onBack = { navController.popBackStack() },
                        photos = photos,
                        onPhotosSelected = { uri ->
                            val internalUri = saveImageToInternalStorage(context, uri)
                            if (internalUri != null) {
                                val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                                scope.launch {
                                    workoutDao.insertPhoto(ProgressPhoto(imageUri = internalUri.toString(), date = today))
                                }
                            } else {
                                Toast.makeText(context, "Fotoğraf kaydedilemedi", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDeletePhoto = { photo: ProgressPhoto ->
                            scope.launch {
                                workoutDao.deletePhoto(photo)
                            }
                        },
                        onMeasurementSave = { m: BodyMeasurement -> scope.launch { workoutDao.insertMeasurement(m) } }
                    )
                }

                composable(Screen.CreateProgram.route) {
                    CreateProgramScreen(
                        existingProgram = editingProgram, // State'i gönderiyoruz
                        onSave = { updatedProgram ->
                            scope.launch {
                                if (updatedProgram.id.toInt() != 0) {
                                    workoutDao.updateProgram(updatedProgram) // DAO'da @Update olmalı
                                } else {
                                    workoutDao.insertProgram(updatedProgram)
                                }
                                editingProgram = null // Temizle
                                navController.popBackStack()
                            }
                        },
                        onBack = {
                            editingProgram = null // Temizle
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.Workout.route) {
                    val nextIdx = WorkoutPrefs.getNextProgramIndex(context, programs.size)
                    val activeProgram = if (programs.isNotEmpty()) programs.getOrNull(nextIdx) else null

                    WorkoutScreen(
                        program = activeProgram,
                        onBack = { navController.popBackStack() },
                        onFinish = { newPRs ->
                            scope.launch {
                                newPRs.forEach { record ->
                                    workoutDao.insertOrUpdateRecord(record)
                                }
                                WorkoutPrefs.completeWorkout(context, programs.size)
                            }
                        },
                        allRecords = pr
                    )
                }
            }
        }
    }
}











