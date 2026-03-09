package com.example.gymtracking

import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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

// Fotoğrafı uygulamanın dahili depolama alanına kopyalar
fun saveImageToInternalStorage(context: android.content.Context, uri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "progress_${System.currentTimeMillis()}.jpg"
        val file = java.io.File(context.filesDir, fileName)
        val outputStream = java.io.FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun MotivationCard() {
    val quotes = listOf(
        "Bugün yapamadıkların, yarınki sınırlarını belirler.",
        "Acı geçicidir, ama başarı sonsuzdur.",
        "Vücudun her şeyi yapabilir, ikna etmen gereken zihnindir.",
        "Disiplin, ne istediğin ile en çok ne istediğin arasındaki seçimdir."
    )
    val randomQuote = remember { quotes.random() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "\"$randomQuote\"",
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun QuickStatsRow(lastWeight: String, totalPRs: Int, lastRecordname: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Son Kilo", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("$lastWeight kg", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("En Son Rekor", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(if(totalPRs==0)"Yok" else "$totalPRs kg", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = lastRecordname,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
fun calculateInitialDelay(): Long {
    val calendar = Calendar.getInstance()
    val now = calendar.timeInMillis
    calendar.set(Calendar.HOUR_OF_DAY, 14) // Akşam 20:00
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)

    if (calendar.timeInMillis <= now) {
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }
    return calendar.timeInMillis - now
}

@Composable
fun LastProgressPreview(lastPhotoUri: String?) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Son Gelişim Fotoğrafın", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(top = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (lastPhotoUri != null) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(if (lastPhotoUri != null) 6.dp else 0.dp)
        ) {
            if (lastPhotoUri != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = lastPhotoUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.2f),
                        contentScale = ContentScale.Crop
                    )
                    AsyncImage(
                        model = lastPhotoUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.defimage),
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Henüz fotoğraf yok", color = Color.Gray)
                    }
                }
            }
        }
    }
}

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

    LaunchedEffect(Unit) {
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
                composable(Screen.Home.route) {
                    var totalDays by remember { mutableIntStateOf(WorkoutPrefs.getTotalDaysCount(context)) }
                    var isFinishedToday by remember { mutableStateOf(WorkoutPrefs.isWorkoutFinishedToday(context)) }
                    var nextIdx by remember { mutableIntStateOf(WorkoutPrefs.getNextProgramIndex(context, programs.size)) }

                    LaunchedEffect(programs) {
                        WorkoutPrefs.checkAndShiftDay(context, programs.size)
                        totalDays = WorkoutPrefs.getTotalDaysCount(context)
                        isFinishedToday = WorkoutPrefs.isWorkoutFinishedToday(context)
                        nextIdx = WorkoutPrefs.getNextProgramIndex(context, programs.size)
                    }

                    val lastPR = pr.lastOrNull()
                    val activeProgram = if (programs.isNotEmpty()) programs.getOrNull(nextIdx) else null

                    HomeScreen(
                        totalDays = totalDays,
                        activeProgram = activeProgram,
                        isFinishedToday = isFinishedToday,
                        programs = programs,
                        navController = navController,
                        userProfile = userProfile,
                        photos = photos,
                        lastRecord = lastPR,
                        onProfileSave = { profile: UserProfile ->
                            scope.launch {
                                val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                                workoutDao.insertMeasurement(BodyMeasurement(weight = profile.weight, bodyFat = profile.bodyFat, date = today))
                            }
                        }
                    )
                }

                composable(Screen.Programs.route) {
                    ProgramsScreen(
                        programs = programs,
                        onAddNew = { navController.navigate(Screen.CreateProgram.route) },
                        onDelete = { program: WorkoutProgram ->
                            scope.launch {
                                workoutDao.deleteProgram(program)
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Progress.route) {
                    ProgressScreen(
                        records = pr,
                        measurements = bodyMeasurements,
                        userProfile = userProfile,
                        onBack = { navController.popBackStack() },
                        photos = photos,
                        onPhotosSelected = { uri: Uri ->
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
                        onSave = { newProg: WorkoutProgram ->
                            scope.launch { workoutDao.insertProgram(newProg); navController.popBackStack() }
                        },
                        onBack = { navController.popBackStack() }
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
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_workout),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "GYM TRACKING",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(Screen.Home, Screen.Programs, Screen.Workout, Screen.Progress)
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    when (val icon = screen.icon) {
                        is Int -> Icon(painterResource(id = icon), contentDescription = screen.title, modifier = Modifier.size(24.dp))
                        is ImageVector -> Icon(icon, contentDescription = screen.title)
                        else -> Icon(Icons.Default.Info, null)
                    }
                },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.Black,
                    indicatorColor = Color.Black
                )
            )
        }
    }
}

@Composable
fun HomeScreen(
    totalDays: Int,
    activeProgram: WorkoutProgram?,
    isFinishedToday: Boolean,
    programs: List<WorkoutProgram>,
    navController: NavHostController,
    userProfile: UserProfile?,
    photos: List<ProgressPhoto>,
    lastRecord: PersonelRecord?,
    onProfileSave: (UserProfile) -> Unit
) {
    var startWeight by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp)) {
        
        val dayTitle = if (userProfile == null || activeProgram == null) "Hoş Geldin! 👋" else "$totalDays. Gün 👋"
        
        val currentOrder = if (activeProgram != null) (programs.indexOf(activeProgram) + 1) else 0
        val daySubtitle = if (userProfile == null || activeProgram == null) {
            "Hadi profilini oluşturalım."
        } else if (isFinishedToday || activeProgram?.istRestDay == true) {
            if (activeProgram?.istRestDay == true) "Bugün Dinlenme Günü! Vücudunu dinlendir. 🧘"
            else "Bugünkü antrenmanını tamamladın! Yarın görüşürüz. ✅"
        } else {
            "Bugünkü: ${activeProgram?.name ?: "Antrenman"} (Hedef: $currentOrder/${programs.size})"
        }

        Text(text = dayTitle, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = daySubtitle, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(32.dp))

        if (userProfile != null) {
            if (activeProgram != null) {
                var isExpanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clickable { if (!activeProgram.istRestDay) isExpanded = !isExpanded },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(text = activeProgram.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                if (activeProgram.istRestDay) {
                                    Text(text = "Bugün Dinlenme!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                                } else {
                                    Text(text = "${activeProgram.exercises.size} Hareket")
                                }
                            }
                            if (!activeProgram.istRestDay) {
                                Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        }
                        if (isExpanded && !activeProgram.istRestDay) {
                            activeProgram.exercises.forEach { ex ->
                                Text("${ex.name}: ${ex.sets}x${ex.reps}", modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            } else {
                Text("Profilin hazır! Şimdi alt menüden programını oluştur.", fontStyle = FontStyle.Italic)
            }

            Spacer(modifier = Modifier.height(40.dp))
            MotivationCard()
            QuickStatsRow(lastWeight = userProfile.weight, totalPRs = lastRecord?.maxKg?.toIntOrNull() ?: 0, lastRecordname = lastRecord?.name ?: "")
            LastProgressPreview(lastPhotoUri = photos.lastOrNull()?.imageUri)
            Spacer(modifier = Modifier.height(35.dp))
            
            if(!isFinishedToday && activeProgram?.istRestDay == false){
                Button(
                    onClick = {
                        if (programs.isNotEmpty()) navController.navigate(Screen.Workout.route)
                        else navController.navigate(Screen.CreateProgram.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text(text = if (programs.isNotEmpty()) "Antrenmana Başla" else "Hadi Başlayalım", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

        } else {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = "Profilini Tamamla", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = startWeight, onValueChange = { startWeight = it }, label = { Text("Kilonuz (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = bodyFat, onValueChange = { bodyFat = it }, label = { Text("Yağ Oranınız (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Button(onClick = { if (startWeight.isNotBlank() && bodyFat.isNotBlank()) onProfileSave(UserProfile(startWeight, bodyFat, 1)) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Kaydet") }
                }
            }
        }
    }
}

@Composable
fun ProgramsScreen(
    programs: List<WorkoutProgram>,
    onAddNew: () -> Unit,
    onDelete: (WorkoutProgram) -> Unit,
    onBack: () -> Unit
) {
    // Dialog durumunu ve silinecek programı takip eden state'ler
    var showDeleteDialog by remember { mutableStateOf(false) }
    var programToDelete by remember { mutableStateOf<WorkoutProgram?>(null) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") }
                Text(text = "Programlarım", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onAddNew) { Icon(Icons.Default.Add, "Yeni Program") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (programs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Henüz program oluşturmadın.", textAlign = TextAlign.Center, color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(programs) { program ->
                    var isExpanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .clickable { if (!program.istRestDay) isExpanded = !isExpanded },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = program.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    if (program.istRestDay) {
                                        Text(text = "Dinlenme Günü", fontSize = 14.sp, color = Color(0xFF4CAF50))
                                    } else {
                                        Text(text = "${program.exercises.size} Hareket", fontSize = 14.sp, color = Color.Gray)
                                    }
                                }

                                Row {
                                    if (!program.istRestDay) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )
                                    }
                                    // SİLME BUTONU GÜNCELLEMESİ
                                    IconButton(onClick = {
                                        programToDelete = program
                                        showDeleteDialog = true
                                    }) {
                                        Icon(Icons.Default.Delete, "Sil", tint = Color.Red)
                                    }
                                }
                            }

                            if (isExpanded && !program.istRestDay) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = Color.LightGray)
                                program.exercises.forEach { ex ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = ex.name, fontSize = 15.sp)
                                        Text(text = "${ex.sets}x${ex.reps}", fontSize = 14.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ONAY ALERTI (Silmek istediğinize emin misiniz?)
    if (showDeleteDialog && programToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Programı Sil", fontWeight = FontWeight.Bold, color = Color.Black) },
            text = { Text(text = "${programToDelete?.name} programını silmek istediğinize emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                Button(
                    onClick = {
                        programToDelete?.let { onDelete(it) }
                        showDeleteDialog = false
                        programToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sil", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    programToDelete = null
                }) {
                    Text("İptal", color = Color.Black)
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgressScreen(
    records: List<PersonelRecord>,
    measurements: List<BodyMeasurement>,
    userProfile: UserProfile?,
    onBack: () -> Unit,
    photos: List<ProgressPhoto>,
    onPhotosSelected: (Uri) -> Unit,
    onDeletePhoto: (ProgressPhoto) -> Unit,
    onMeasurementSave: (BodyMeasurement) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var newWeight by remember { mutableStateOf("") }
    var newFat by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onPhotosSelected(it) }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)
        .verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") }
            Text(text = "Gelişimim", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Kişisel Rekorlar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (records.isEmpty()) {
            Text(text = "Henüz rekor yok.", fontStyle = FontStyle.Italic, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        } else {
            records.forEach { record ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)) {
                    Row(modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = record.name, fontWeight = FontWeight.Medium)
                            Text(text = record.date, fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(text = "${record.maxKg} kg", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Vücut Gelişimi", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Yeni Ölçüm Ekle") }
        measurements.forEach { measure ->
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)) {
                Row(modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "Kilo: ${measure.weight} kg", fontWeight = FontWeight.Bold)
                        Text(text = measure.date, fontSize = 12.sp, color = Color.Gray)
                    }
                    Text(text = "Yağ: %${measure.bodyFat}", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Fotoğraf Günlüğü", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                // Fotoğraf Ekleme Kartı
                Card(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.size(width = 150.dp, height = 200.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp))
                            Text("Foto Ekle", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            items(photos) { photo ->
                // Mevcut Fotoğraflar
                Card(
                    modifier = Modifier
                        .size(width = 150.dp, height = 200.dp)
                        .combinedClickable(
                            onClick = { /* Fotoğrafı büyütmek istersen buraya */ },
                            onLongClick = { onDeletePhoto(photo) }
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Fotoğrafın Kendisi
                        AsyncImage(
                            model = photo.imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Alt Kısımdaki Tarih Bandı
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f)) // Yarı saydam siyah arka plan
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = photo.date, // ProgressPhoto modelindeki date alanı
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Yeni Ölçüm Gir") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newWeight, onValueChange = { newWeight = it }, label = { Text("Kilo (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = newFat, onValueChange = { newFat = it }, label = { Text("Yağ Oranı (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newWeight.isNotBlank() && newFat.isNotBlank()) {
                        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                        onMeasurementSave(BodyMeasurement(weight = newWeight, bodyFat = newFat, date = today))
                        showDialog = false
                    }
                }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Kaydet") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("İptal", color = Color.Black) } }
        )
    }
}

@Composable
fun CreateProgramScreen(onSave: (WorkoutProgram) -> Unit, onBack: () -> Unit) {
    var programName by remember { mutableStateOf("") }
    var isRestDay by remember { mutableStateOf(false) }
    val exercises = remember { mutableStateListOf<Exercise>() }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)
        .verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") }
            Text(text = "Yeni Program", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Checkbox(
                checked = isRestDay,
                onCheckedChange = {
                    isRestDay = it
                    if (it) {
                        programName = "Dinlenme Günü"
                        exercises.clear()
                    } else {
                        programName = ""
                    }
                },
                colors = CheckboxDefaults.colors(checkedColor = Color.Black)
            )
            Text("Bu bir dinlenme günü")
        }

        OutlinedTextField(
            value = programName,
            onValueChange = { if (!isRestDay) programName = it },
            label = { Text("Program Adı (Örn: Göğüs & Kol)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRestDay
        )

        if (!isRestDay) {
            Text(text = "Egzersizler", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
            exercises.forEachIndexed { index, ex ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = ex.name,
                            onValueChange = { exercises[index] = ex.copy(name = it) },
                            label = { Text("Egzersiz Adı") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = ex.sets,
                                onValueChange = { exercises[index] = ex.copy(sets = it) },
                                label = { Text("Set") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = ex.reps,
                                onValueChange = { exercises[index] = ex.copy(reps = it) },
                                label = { Text("Tekrar") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }
            }
            Button(onClick = { exercises.add(Exercise("", "", "")) }, shape = RoundedCornerShape(12.dp) ,modifier = Modifier.padding(vertical = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                Text("Egzersiz Ekle")
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.05f))
            ) {
                Text(
                    text = "Dinlenme günlerinde egzersiz eklenmez. Vücudunu dinlendir!",
                    modifier = Modifier.padding(16.dp),
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        val isFormValid = if (isRestDay) {
            programName.isNotBlank()
        } else {
            programName.isNotBlank() &&
                    exercises.isNotEmpty() &&
                    exercises.all { it.name.isNotBlank() && it.sets.isNotBlank() && it.reps.isNotBlank() }
        }
        Button(
            onClick = {
                if (isFormValid) {
                    onSave(WorkoutProgram(
                        name = programName,
                        istRestDay = isRestDay,
                        exercises = if (isRestDay) emptyList() else exercises.toList()
                    ))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = isFormValid,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Programı Kaydet")
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun WorkoutScreen(program: WorkoutProgram?, onBack: () -> Unit, onFinish: (List<PersonelRecord>) -> Unit) {
    if (program == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Antrenman yükleniyor...")
                Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Geri Dön") }
            }
        }
        return
    }

    val context = LocalContext.current
    var isFinishedToday by remember { mutableStateOf(WorkoutPrefs.isWorkoutFinishedToday(context)) }
    val maxWeightMap = remember(program.id) { mutableStateMapOf<Int, String>() }
    val completedMap = remember(program.id) { mutableStateMapOf<Int, Boolean>() }
    // En az bir egzersiz işaretlenmiş mi kontrolü
    // program.exercises listesindeki her bir indeksin completedMap'te true olup olmadığını kontrol eder
    val areAllExercisesCompleted = program.exercises.indices.all { exIndex ->
        completedMap[exIndex] == true
    }

    if (isFinishedToday || program.istRestDay) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (program.istRestDay) Icons.Default.Info else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (program.istRestDay) "Dinlenme Günü" else "Tebrikler!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (program.istRestDay) "Bugün dinlenme gününüz. Vücudunu dinlendiririn ve yarınki antrenmana hazırlanın." else "Bugünün antrenmanını başarıyla tamamladınız.",
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("Ana Menüye Dön")
                    }
                }
            }
        }
    } else {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") }
                Text(text = program.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            program.exercises.forEachIndexed { exIndex, ex ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(text = ex.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = "${ex.sets} Set x ${ex.reps} Tekrar", fontSize = 14.sp, color = Color.Gray)
                            }
                            Checkbox(checked = completedMap[exIndex] ?: false, onCheckedChange = { completedMap[exIndex] = it }, colors = CheckboxDefaults.colors(checkedColor = Color.Black))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = maxWeightMap[exIndex] ?: "",
                            onValueChange = {
                                maxWeightMap[exIndex] = it
                                if (it.isNotBlank()) completedMap[exIndex] = true
                            },
                            label = { Text("Basılan Kilo (kg)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                    val newPRs = mutableListOf<PersonelRecord>()

                    // Sadece işaretlenmiş (tamamlanmış) hareketleri dönüyoruz
                    completedMap.forEach { (index, isChecked) ->
                        if (isChecked) {
                            val weight = maxWeightMap[index]
                            if (!weight.isNullOrBlank()) {
                                newPRs.add(
                                    PersonelRecord(
                                        name = program.exercises[index].name,
                                        maxKg = weight,
                                        date = today
                                    )
                                )
                            }
                        }
                    }

                    onFinish(newPRs)
                    isFinishedToday = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                // ÖNEMLİ: Eğer hiçbir checkbox işaretli değilse buton tıklanamaz olur
                enabled = areAllExercisesCompleted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f) // Pasifken gri görünür
                )
            ) {
                Text(
                    text = "Antrenmanı Bitir",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (areAllExercisesCompleted) Color.White else Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
