package com.example.gymtracking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import kotlin.collections.emptyList
import java.util.UUID

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
    val exercises: List<Exercise>
)

data class Hareketler(
    val name: String,
    val done: MutableState<Boolean> = mutableStateOf(false),
    val maxKg: MutableState<String> = mutableStateOf("")
)

data class PersonelRecord(
    val name: String,
    val maxKg: String
)

data class ProgressPhoto(
    val image: Int,
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
    object Workout : Screen("workout", "Antreman", R.drawable.ic_workout)
    object Progress : Screen("progress", "Progress", R.drawable.ic_progress)
    object CreateProgram : Screen("create_program", "Yeni Program", Icons.Default.Add)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val programs = remember {
        mutableStateListOf(
            WorkoutProgram(name = "Push", exercises = emptyList()),
            WorkoutProgram(name = "Pull", exercises = emptyList()),
            WorkoutProgram(name = "Leg", exercises = emptyList()),
            WorkoutProgram(name = "Full Body", exercises = emptyList())
        )
    }
    val pr = remember {
        mutableStateListOf<PersonelRecord>()
        /*
        mutableStateListOf(
            PersonelRecord(name = "Bench Press", maxKg = "80"),
            PersonelRecord(name = "Squat", maxKg = "100"),
            PersonelRecord(name = "Deadlift", maxKg = "120")
        )

         */
    }

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
            composable(Screen.Home.route) { HomeScreen(
                programs=programs,
                photos=emptyList()
            ) }
            
            composable(Screen.Programs.route) {
                ProgramsScreen(
                    programs = programs,
                    onAddNew = { navController.navigate(Screen.CreateProgram.route) },
                    onEdit = { program ->  /* TODO */ },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Progress.route) {
                ProgressScreen(
                    records = pr,
                    onBack = { navController.popBackStack() }
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
                WorkoutScreen(
                    onBack = { navController.popBackStack() }
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
fun HomeScreen(programs: List<WorkoutProgram>, photos: List<ProgressPhoto>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        val isFirstTime=programs.isEmpty()
        val dayTitle = if (isFirstTime) "Hoş Geldin! 👋" else "👋 42. Gün" //42.gün kısmı room databaseden çekilecek
        val daySubTitle = if (isFirstTime) "Hadi Programlar sayfasına git ve program ekle!" else "Bu hafta: 3/5 gün" //Databaseden çekilecek

        Text(text = dayTitle, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = daySubTitle, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 4.dp))
        Spacer(modifier = Modifier.height(32.dp))
        if (photos.isNotEmpty()){
            Text(text = "Son eklenen foto", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.size(120.dp)) {
                Image(
                    painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

        }

        Text(text = "Bugünün programı", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        if (programs.isNotEmpty()){
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = programs.first().name, fontSize = 20.sp, fontWeight = FontWeight.Bold) //databaseden çekilecek
                    Text(text = "${programs.first().exercises.size} Hareket • 45-60 Dakika", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 6.dp)) //databaseden çekilecek
                }
            }
        }
        else {
            //boş durum kartı
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)

            ) {
                Text(text = "Henüz bir program oluşturmadın.", modifier = Modifier.padding(20.dp),textAlign = TextAlign.Center, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(16.dp)) {
            Text(text = "Antrenmana Başla", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProgramsScreen(programs: MutableList<WorkoutProgram>, onAddNew: () -> Unit, onBack: () -> Unit,onEdit: (WorkoutProgram) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri") }
                Text(text = "Programlarım", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // EĞER PROGRAM VARSA LİSTELE

            if (programs.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(programs) { program ->
                        // Her kartın kendi genişleme durumunu tutan değişken
                        var isExpanded by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(), // Genişleme animasyonu ekler
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            onClick = { isExpanded = !isExpanded } // Tıklayınca genişlet/daralt
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Üst Kısım: Başlık ve İşlem Butonları
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = program.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "${program.exercises.size} Hareket", color = MaterialTheme.colorScheme.secondary)
                                    }

                                    // Düzenle Butonu
                                    IconButton(onClick = { onEdit(program) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    // Sil Butonu
                                    IconButton(onClick = { programs.remove(program) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                // Detay Kısmı (Sadece isExpanded true ise görünür)
                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    program.exercises.forEach { exercise ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = exercise.name, fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f))
                                            Text(
                                                text = "${exercise.sets} Set x ${exercise.reps} Tekrar",
                                                modifier = Modifier.weight(1.5f),
                                                textAlign = TextAlign.End,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }else {
                // EĞER PROGRAM YOKSA (BOŞ DURUM)
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Henüz bir program eklemedin. Sağ alttaki butondan yeni bir program oluşturabilirsin.",
                            modifier = Modifier.padding(24.dp),
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // YENİ PROGRAM EKLE BUTONU
        ExtendedFloatingActionButton(
            onClick = onAddNew,
            icon = { Icon(Icons.Default.Add, "Ekle") },
            text = { Text("Yeni Program") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun CreateProgramScreen(onSave: (WorkoutProgram) -> Unit, onBack: () -> Unit) {
    var programName by remember { mutableStateOf("") }
    val exercises = remember { mutableStateListOf(Exercise()) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") }
            Text(text = "Yeni Program Oluştur", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = programName, onValueChange = { programName = it }, label = { Text("Program Adı") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Hareketler", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        exercises.forEachIndexed { index, exercise ->
            Card(modifier = Modifier.padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(value = exercise.name, onValueChange = { exercises[index] = exercise.copy(name = it) }, label = { Text("Hareket Adı") }, modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = exercise.sets, onValueChange = { exercises[index] = exercise.copy(sets = it) }, label = { Text("Set") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = exercise.reps, onValueChange = { exercises[index] = exercise.copy(reps = it) }, label = { Text("Tekrar") }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        TextButton(onClick = { exercises.add(Exercise()) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(4.dp))
            Text("Hareket Ekle")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { if (programName.isNotBlank()) onSave(WorkoutProgram(name = programName, exercises = exercises.toList())) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)) {
            Text("Programı Kaydet")
        }
    }
}

@Composable
fun WorkoutScreen(onBack: () -> Unit) {

    val exercises = remember {
        mutableStateListOf<Hareketler>()
        /*
        mutableStateListOf(
            Hareketler("Bench Press"),
            Hareketler("Deadlift"),
            Hareketler("Squat")
        )

         */
    }



    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") }
            Text(text = "Bugünkü Antremanım", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (exercises.isNotEmpty()) {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(exercises) { exercise ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = exercise.done.value, onCheckedChange = { exercise.done.value = it })
                                Text(text = exercise.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(text = "3/5", fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                            if (exercise.done.value) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(value = exercise.maxKg.value, onValueChange = { exercise.maxKg.value = it }, label = { Text("En Fazla Kilogram") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            }
                        }
                    }
                }
            }
            Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(56.dp), shape = RoundedCornerShape(12.dp)) {
                Text(text = "Antremanı Tamamla", fontSize = 18.sp)
            }

        }
        else{
            // EĞER PROGRAM YOKSA (BOŞ DURUM)
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Henüz bir Program Oluşturmamışsın! Lütfen Programramlar sayfasına git ve program ekle.",
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        }


    }
}

@Composable
fun ProgressScreen(onBack: () -> Unit, records: List<PersonelRecord>) {
    val photos = remember {
        mutableStateListOf<ProgressPhoto>() // boş durum için boş liste alttaki listeleri silip boş durumu görebilirsin.
        /*
        mutableStateListOf(
            ProgressPhoto(android.R.drawable.ic_menu_gallery, "12.02.2024"),
            ProgressPhoto(android.R.drawable.ic_menu_gallery, "15.02.2024"),
            ProgressPhoto(android.R.drawable.ic_menu_gallery, "18.02.2024")
        )

         */
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri") }
            Text(text = "Gelişimim", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Üst Yarı: Kişisel Rekorlar (%50)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Kişisel Rekorlar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if(records.isNotEmpty()){
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(records) { record ->
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = record.name, fontSize = 16.sp)
                                Text(text = "${record.maxKg} kg", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
            else{
                //rekorlar boş durum
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Henüz bir PR eklemedin.", textAlign = TextAlign.Center, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.secondary,)
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alt Yarı: Fotoğraf Günlüğü (%50)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Fotoğraf Günlüğü", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // FOTOĞRAF EKLE BUTONU (İlk item)
                item {
                    Card(
                        onClick = { /* TODO: Fotoğraf seçme işlemi */ },
                        modifier = Modifier.width(200.dp).fillMaxHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(48.dp))
                            Text(text = "Foto Ekle", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // MEVCUT FOTOĞRAFLAR
                if(photos.isNotEmpty()){
                    items(photos) { photo ->
                        Card(
                            modifier = Modifier.width(200.dp).fillMaxHeight(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    painter = painterResource(id = photo.image),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Surface(
                                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = photo.date,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                }
                else{
                    item{
                        Box(
                            modifier = Modifier.width(150.dp).fillMaxHeight(),
                            contentAlignment = Alignment.Center

                        ) {
                            Text(text = "İlk Fotoğrafını Ekle!.", textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.secondary,
                                )
                        }
                    }
                }

            }
        }
    }
}
