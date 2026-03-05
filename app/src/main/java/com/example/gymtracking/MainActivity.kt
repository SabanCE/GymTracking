package com.example.gymtracking

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType

// Veri Modelleri
data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var sets: String = "",
    var reps: String = ""
)

data class WorkoutProgram(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val exercises: List<Exercise>,
    val istRestDay: Boolean = false
)

data class Hareketler(
    val displayName: String, // Ekranda görünecek (örn: Bench Press (3x10))
    val originalName: String, // Kaydedilecek (örn: Bench Press)
    val done: MutableState<Boolean> = mutableStateOf(false),
    val maxKg: MutableState<String> = mutableStateOf("")
)

data class PersonelRecord(
    val name: String,
    val maxKg: String,
    val date: String // Kayıt tarihi eklendi
)

data class ProgressPhoto(
    val image: Int,
    val date: String
)
data class UserProfile(
    val weight: String = "",
    val bodyFat: String = "",
    val startDay: Int = 1
)
data class BodyMeasurement(
    val weight: String,
    val bodyFat: String,
    val date: String
)

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
    var selectedWorkoutProgram by remember { mutableStateOf<WorkoutProgram?>(null) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    val programs = remember { mutableStateListOf<WorkoutProgram>() }
    val pr = remember { mutableStateListOf<PersonelRecord>() }
    val bodyMeasurements = remember { mutableStateListOf<BodyMeasurement>() }

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
                HomeScreen(
                    programs = programs,
                    photos = emptyList(),
                    navController = navController,
                    userProfile = userProfile, // MainScreen'deki state
                    onProfileSave = { profile -> userProfile = profile }, // Veri gelince state'i güncelle
                    onMeasurementSave = { measurement -> bodyMeasurements.add(measurement) } // Veri gelince state'e ekle
                    )
            }

            composable(Screen.Programs.route) {
                ProgramsScreen(
                    programs = programs,
                    onAddNew = { navController.navigate(Screen.CreateProgram.route) },
                    onDelete = { program ->
                        if (selectedWorkoutProgram?.id == program.id) {
                            selectedWorkoutProgram = null
                        }
                    },
                    onEdit = { /* Düzenleme */ },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Progress.route) {
                ProgressScreen(
                    records = pr,
                    onBack = { navController.popBackStack() },
                    measurements = bodyMeasurements,
                    userProfile = userProfile,
                )
            }

            composable(Screen.CreateProgram.route) {
                CreateProgramScreen(
                    onSave = { newProgram ->
                        programs.add(newProgram)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Workout.route) {
                if (selectedWorkoutProgram == null && programs.isNotEmpty()) {
                    selectedWorkoutProgram = programs.first()
                }
                WorkoutScreen(
                    program = selectedWorkoutProgram,
                    onBack = { navController.popBackStack() },
                    onFinish = { newPRs ->
                        pr.addAll(newPRs)
                    }
                )
            }
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
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    programs: List<WorkoutProgram>,
    photos: List<ProgressPhoto>,
    navController: NavHostController,
    userProfile: UserProfile?,
    onProfileSave: (UserProfile) -> Unit,
    onMeasurementSave: (BodyMeasurement) -> Unit
) {
    val context = LocalContext.current
    var startWeight by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }
    var currentDay by remember { mutableStateOf("1") }

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp)) {

        val dayTitle = if (userProfile == null) "Hoş Geldin! 👋" else "${userProfile.startDay}. Gün 👋"
        val daySubtitle = if (userProfile == null) "Hadi profilini oluşturalım." else "Bu hafta hedef: 1/7 Gün"

        Text(text = dayTitle, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = daySubtitle, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(32.dp))

        if (userProfile != null) {
            if (programs.isNotEmpty()) {
                val dailyProgram = programs.first()
                var isExpanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clickable { isExpanded = !isExpanded },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(text = dailyProgram.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                if (dailyProgram.istRestDay) {
                                    Text(text = "Bugün Dinlenme!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                                } else {
                                    Text(text = "${dailyProgram.exercises.size} Hareket")
                                }
                            }
                            if (!dailyProgram.istRestDay) {
                                Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        }
                        if (isExpanded && !dailyProgram.istRestDay) {
                            dailyProgram.exercises.forEach { ex ->
                                Text("${ex.name}: ${ex.sets}x${ex.reps}", modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            } else {
                Text("Profilin hazır! Şimdi alt menüden programını oluştur.", fontStyle = FontStyle.Italic)
            }

            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = {
                    if (programs.isNotEmpty()) navController.navigate(Screen.Workout.route)
                    else navController.navigate(Screen.CreateProgram.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = if (programs.isEmpty()) "Programını Oluştur" else "Antremana Başla", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        else {
            Text(text = "Kişisel Verilerim:", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = startWeight, onValueChange = { startWeight = it }, label = { Text("Mevcut Kilo (kg)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = bodyFat, onValueChange = { bodyFat = it }, label = { Text("Yağ Oranı (%)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = currentDay, onValueChange = { currentDay = it }, label = { Text("Kaçıncı Gündesin?") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                    Button(
                        onClick = {
                            if (startWeight.isNotBlank() && bodyFat.isNotBlank()) {
                                val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                                onProfileSave(UserProfile(startWeight, bodyFat, currentDay.toIntOrNull() ?: 1))
                                onMeasurementSave(BodyMeasurement(startWeight, bodyFat, today))
                                Toast.makeText(context, "Veriler kaydedildi!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text("Bilgilerimi Kaydet")
                    }
                }
            }
        }
    }
}

@Composable
fun ProgramsScreen(programs: MutableList<WorkoutProgram>, onAddNew: () -> Unit, onBack: () -> Unit, onDelete: (WorkoutProgram) -> Unit, onEdit: (WorkoutProgram) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") }
                Text(text = "Programlarım", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if(programs.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz bir Program bulunmuyor.\nLütfen bir program oluşturun.",
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic,
                        color = Color.Gray
                    )

                }
            }
            else{
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(programs) { program ->
                        var isExpanded by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clickable { if(!program.istRestDay) isExpanded = !isExpanded },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = program.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        if (program.istRestDay) {
                                            Text(text = "Bugün Dinlenme!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                                        } else {
                                            Text(text = "${program.exercises.size} Hareket", color = Color.Gray)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            onDelete(program)
                                            programs.remove(program)
                                        }) {
                                            Icon(Icons.Default.Delete, "Sil", tint = MaterialTheme.colorScheme.error)
                                        }
                                        if (!program.istRestDay) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                                if (isExpanded && !program.istRestDay) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(thickness = 0.5.dp)
                                    program.exercises.forEach { ex ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = ex.name, fontWeight = FontWeight.Medium)
                                            Text(text = "${ex.sets} x ${ex.reps}", color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        ExtendedFloatingActionButton(
            onClick = onAddNew,
            icon = { Icon(Icons.Default.Add, "Ekle") },
            text = { Text("Yeni Program") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )
    }
}

@Composable
fun CreateProgramScreen(onSave: (WorkoutProgram) -> Unit, onBack: () -> Unit) {
    var programName by remember { mutableStateOf("") }
    val exercises = remember { mutableStateListOf(Exercise()) }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
        .verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") }
            Text(text = "Yeni Program Oluştur", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = programName, onValueChange = { programName = it }, label = { Text("Program Adı") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val finalName = if(programName.isNotBlank()) programName else "Dinlenme Günü"
                onSave(WorkoutProgram(name = finalName, exercises = emptyList(), istRestDay = true))
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Icon(Icons.Default.Star, null)
            Spacer(Modifier.width(8.dp))
            Text("Dinlenme Günü Olarak Kaydet")
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

        exercises.forEachIndexed { index, ex ->
            Card(modifier = Modifier.padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(value = ex.name, onValueChange = { exercises[index] = ex.copy(name = it) }, label = { Text("Hareket Adı") })
                    Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = ex.sets, onValueChange = { exercises[index] = ex.copy(sets = it) }, label = { Text("Set") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = ex.reps, onValueChange = { exercises[index] = ex.copy(reps = it) }, label = { Text("Tekrar") }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        TextButton(onClick = { exercises.add(Exercise()) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Icon(Icons.Default.Add, null); Text("Hareket Ekle")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { if (programName.isNotBlank()) onSave(WorkoutProgram(name = programName, exercises = exercises.toList())) }, modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)) {
            Text("Programı Kaydet")
        }
    }
}

@Composable
fun WorkoutScreen(
    program: WorkoutProgram?,
    onBack: () -> Unit,
    onFinish: (List<PersonelRecord>) -> Unit
) {
    val context = LocalContext.current
    var isFinished by remember { mutableStateOf(false) }
    val workoutExercises = remember(program) {
        program?.exercises?.map { exercise ->
            Hareketler(
                displayName = "${exercise.name} (${exercise.sets}x${exercise.reps})",
                originalName = exercise.name,
                done = mutableStateOf(false),
                maxKg = mutableStateOf("")
            )
        }?.toMutableStateList() ?: mutableStateListOf()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") }
            Text(
                text = program?.name ?: "Antrenman",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isFinished) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tebrikler!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Bugünün antremanını tamamlandınız.",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                }
            }
        } else if (program?.istRestDay == true) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Bugün dinlenme günü!\nKeyfine bak.", textAlign = TextAlign.Center, fontSize = 20.sp)
            }
        } else if (workoutExercises.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Henüz bir antrenman hareketi bulunmuyor.\nLütfen bir program seçin veya oluşturun.",
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(workoutExercises) { exercise ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = exercise.done.value,
                                    onCheckedChange = { exercise.done.value = it })
                                Text(text = exercise.displayName, fontSize = 18.sp)
                            }
                            if (exercise.done.value) {
                                OutlinedTextField(
                                    value = exercise.maxKg.value,
                                    onValueChange = { exercise.maxKg.value = it },
                                    label = { Text("Bastığın Kilo (kg)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            Button(
                onClick = {
                    val allDone = workoutExercises.all { it.done.value }
                    if (workoutExercises.isEmpty()) {
                        Toast.makeText(context, "Lütfen bir program  oluşturun!", Toast.LENGTH_LONG)
                            .show()
                        return@Button
                    }
                    if (allDone) {
                        val sdf = SimpleDateFormat("d MMMM", Locale("tr"))
                        val currentDate = sdf.format(Date())

                        val newRecords = workoutExercises
                            .filter { it.maxKg.value.isNotBlank() && it.maxKg.value != "0" }
                            .map { PersonelRecord(it.originalName, it.maxKg.value, currentDate) }

                        Toast.makeText(context, "Antrenman Tamamlandı!", Toast.LENGTH_SHORT).show()
                        isFinished = true
                        onFinish(newRecords)
                    } else {
                        Toast.makeText(context, "Lütfen tüm hareketleri tamamla!", Toast.LENGTH_LONG)
                            .show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Antremanı Tamamla", fontSize = 18.sp)
            }
        }


    }
}

@Composable
fun ProgressScreen(onBack: () -> Unit, records: List<PersonelRecord>, measurements: List<BodyMeasurement>,userProfile: UserProfile?) {
    val photos = remember { mutableStateListOf<ProgressPhoto>() }
    // Ölçüm Dialog'u için kontroller
    var showDialog by remember { mutableStateOf(false) }
    var newWeight by remember { mutableStateOf("") }
    var newFat by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Tüm ekranın dikeyde kayabilmesi için Column'a verticalScroll ekliyoruz
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

        // 1. KISIM: KİŞİSEL REKORLAR
        Text(text = "Kişisel Rekorlar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (records.isEmpty()) {
            // PR Boş Durumu
            Text(
                text = "Henüz bir rekor kaydedilmedi. Antrenmanlarını tamamlayarak rekorlarını burada görebilirsin.",
                fontStyle = FontStyle.Italic,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            // Rekorları alt alta listeliyoruz
            records.forEach { record ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = record.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(text = record.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Text(text = "${record.maxKg} kg", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. KISIM: VÜCUT ÖLÇÜMLERİ (Dikey ve Genişleyen Liste)
        Text(text = "Vücut Gelişimi", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        if(userProfile != null) {
            Card(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yeni Ölçüm Ekle", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }


        if (measurements.isEmpty()) {
            Text("Henüz ölçüm kaydı yok.", fontStyle = FontStyle.Italic, color = Color.Gray)
        } else {
            measurements.forEach { measure ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            // Tarih Formatlama: "15 Temmuz" formatına çevirme
                            val formattedDate = try {
                                val inputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                val outputFormat = SimpleDateFormat("d MMMM", Locale("tr"))
                                measure.date.let { inputFormat.parse(it)?.let { d -> outputFormat.format(d) } } ?: measure.date
                            } catch (e: Exception) {
                                measure.date
                            }

                            Text(text = formattedDate, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Kilo: ${measure.weight} kg", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "Yağ: %${measure.bodyFat}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. KISIM: FOTOĞRAF GÜNLÜĞÜ (Yana Kaydırılabilir)
        Text(text = "Fotoğraf Günlüğü", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(onClick = { /* Foto Ekle */ }, modifier = Modifier.size(150.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp))
                            Text("Foto Ekle", fontSize = 12.sp)
                        }
                    }
                }
            }
            items(photos) { photo ->
                Card(modifier = Modifier.size(150.dp)) {
                    Image(painter = painterResource(id = photo.image), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp)) // En altta rahatlık için boşluk
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Yeni Ölçüm Gir") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newWeight,
                        onValueChange = { newWeight = it },
                        label = { Text("Kilo (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = newFat,
                        onValueChange = { newFat = it },
                        label = { Text("Yağ Oranı (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newWeight.isNotBlank() && newFat.isNotBlank()) {
                        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                        // MainScreen'deki listeye ekle (measurements bir MutableList olmalı)
                        (measurements as? MutableList)?.add(BodyMeasurement(newWeight, newFat, today))

                        showDialog = false
                        newWeight = ""
                        newFat = ""
                        Toast.makeText(context, "Ölçüm kaydedildi!", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("İptal") }
            }
        )
    }
}
