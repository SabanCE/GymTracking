package com.example.gymtracking

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import android.content.Context
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // isFinishedToday parametresini takip eden yerel bir state
    var localIsFinishedToday by remember(isFinishedToday) { mutableStateOf(isFinishedToday) }
    Box(modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)) {

            val dayTitle = if (programs.isEmpty()) "Hoş Geldin! 👋" else "$totalDays. Gün 👋"

            val currentOrder = if (activeProgram != null) (programs.indexOf(activeProgram) + 1) else 0
            val daySubtitle = if (programs.isEmpty()) {
                "Hadi programını oluşturalım."
            } else if (localIsFinishedToday || activeProgram?.istRestDay == true) {
                if (activeProgram?.istRestDay == true) "Bugün Dinlenme Günü! Vücudunu dinlendir. 🧘"
                else "Bugünkü antrenmanını tamamladın! Yarın görüşürüz. ✅\nHedef: $currentOrder/${programs.size}"
            } else {
                "Bugünkü: ${activeProgram?.name ?: "Antrenman"} (Hedef: $currentOrder/${programs.size})"
            }

            Text(text = dayTitle, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(text = daySubtitle, fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            // Dinlenme gününü atla butonu: İndeksi ilerletir ama günü (totalDays) ve bitirilme durumunu değiştirmez.
            if (activeProgram?.istRestDay == true && !localIsFinishedToday) {
                Button(
                    onClick = {
                        scope.launch {
                            // Sadece program indeksini ilerlet, gün sayısını ve tamamlanma durumunu değiştirme
                            WorkoutPrefs.forceNextProgram(context, programs.size)
                            
                            // UI'ı güncellemek için tetikleyici
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Dinlenme Gününü Atla ve Devam Et", fontWeight = FontWeight.Bold)
                }
            }

            // hasMissed kontrolüne yeni yerel state'i de ekliyoruz
            // hasMissed kontrolüne yeni kalıcı kontrolü ekliyoruz
            val isWarningAlreadyDismissed = WorkoutPrefs.isWarningDismissedToday(context)
            val hasMissed = remember(programs, totalDays, localIsFinishedToday) {
                WorkoutPrefs.hasMissedWorkout(context)
            }

            // ŞART: Kaçırılan antrenman varsa VE bugün bitmediyse VE bugün henüz "kapat" denmediyse
            if (hasMissed && !localIsFinishedToday && !isWarningAlreadyDismissed) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kaçırılan Antrenman!", fontWeight = FontWeight.ExtraBold, color = Color.Red, fontSize = 16.sp)
                        }

                        Text(
                            text = "Dünkü antrenmanını tamamlamadın. Kaldığın yerden devam etmek ister misin?",
                            fontSize = 14.sp, modifier = Modifier.padding(vertical = 10.dp), color = Color.DarkGray
                        )

                        // SEÇENEK 1: DÜNKÜ ANTRENMANA DÖN
                        Button(
                            onClick = {
                                scope.launch {
                                    val prevIndex = WorkoutPrefs.getPreviousProgramIndex(context, programs.size)
                                    // KALICI OLARAK KAPAT
                                    WorkoutPrefs.dismissWarning(context)

                                    context.getSharedPreferences("workout_prefs", Context.MODE_PRIVATE)
                                        .edit()
                                        .putInt("current_program_index", prevIndex)
                                        .putInt("total_days_count", (totalDays - 1).coerceAtLeast(1))
                                        .apply()

                                    navController.navigate(Screen.Workout.route)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Dünkü Antrenmana Dön", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // SEÇENEK 2: BUGÜNKÜ PROGRAMLA DEVAM ET
                        TextButton(
                            onClick = {
                                scope.launch {
                                    // KALICI OLARAK KAPAT (Antrenmanı bitirmez, sadece kartı yok eder)
                                    WorkoutPrefs.dismissWarning(context)

                                    // UI'ı anında güncellemek için ana sayfayı tetikle
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Hayır, Bugünün Programıyla Devam Et", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

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
            } else if (programs.isEmpty()) {
                Text("Henüz bir programın yok! Hadi alt menüden programını oluştur.", fontStyle = FontStyle.Italic)
            }

            // Butonun görünme şartı:
            // 1. Bugün antrenman bitmemiş olmalı
            // 2. Dinlenme günü olmamalı
            // 3. YA hasMissed hiç olmamalı YA DA kullanıcı uyarıyı zaten kapatmış/seçimini yapmış olmalı
            if (!localIsFinishedToday && activeProgram?.istRestDay == false && (!hasMissed || isWarningAlreadyDismissed)) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        navController.navigate(Screen.Workout.route)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text(text = "Antrenmana Başla", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            MotivationCard()
            QuickStatsRow(
                lastWeight = userProfile?.weight ?: "0",
                totalPRs = if (lastRecord?.maxKg?.toDoubleOrNull() != null) lastRecord.maxKg.toDouble().toInt() else 0,
                lastRecordname = lastRecord?.name ?: ""
            )
            LastProgressPreview(lastPhotoUri = photos.lastOrNull()?.imageUri)
            Spacer(modifier = Modifier.height(35.dp))
        }

        ExtendedFloatingActionButton(
            onClick = { navController.navigate("macros_screen") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color.Black,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.macro),
                    contentDescription = "Makro",
                    modifier = Modifier.size(20.dp)
                )
            },
            text = {
                Text("Makro Hesapla")
            }
        )
    }
}
