package com.example.gymtracking

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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


    Box(modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)) {

            val dayTitle = if (programs.isEmpty()) "Hoş Geldin! 👋" else "$totalDays. Gün 👋"

            val currentOrder = if (activeProgram != null) (programs.indexOf(activeProgram) + 1) else 0
            val daySubtitle = if (programs.isEmpty()) {
                "Hadi programını oluşturalım."
            } else if (isFinishedToday || activeProgram?.istRestDay == true) {
                if (activeProgram?.istRestDay == true) "Bugün Dinlenme Günü! Vücudunu dinlendir. 🧘"
                else "Bugünkü antrenmanını tamamladın! Yarın görüşürüz. ✅\nHedef: $currentOrder/${programs.size}"
            } else {
                "Bugünkü: ${activeProgram?.name ?: "Antrenman"} (Hedef: $currentOrder/${programs.size})"
            }

            Text(text = dayTitle, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(text = daySubtitle, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)

            Spacer(modifier = Modifier.height(32.dp))

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

            if(!isFinishedToday && activeProgram?.istRestDay == false){
                Spacer(modifier = Modifier.height(24.dp))
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
        // FLOATING ACTION BAR (Manuel Yerleşim)

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
                    modifier = Modifier.size(20.dp)   // ikon küçültme
                )
            },
            text = {
                Text("Makro Hesapla")
            }
        )

    }

}