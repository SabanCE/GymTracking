package com.example.gymtracking

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

@Composable
fun WorkoutScreen(
    program: WorkoutProgram?,
    onBack: () -> Unit,
    onFinish: (List<PersonelRecord>) -> Unit,
    allRecords: List<PersonelRecord>
) {
    if (program == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Antrenman yükleniyor...")
                Button(onClick = onBack, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                    Text("Geri Dön")
                }
            }
        }
        return
    }

    val context = LocalContext.current
    var isFinishedToday by remember { mutableStateOf(WorkoutPrefs.isWorkoutFinishedToday(context)) }

    // Her egzersiz indeksi için: List<Pair<Kilo, Tekrar>> formatında veri tutuyoruz
    val exerciseDetailsMap = remember(program.id) {
        mutableStateMapOf<Int, List<Pair<String, String>>>().apply {
            program.exercises.forEachIndexed { index, ex ->
                val setSize = ex.sets.toIntOrNull() ?: 1
                this[index] = List(setSize) { "" to "" }
            }
        }
    }

    val completedMap = remember(program.id) { mutableStateOf<Map<Int, Boolean>>(emptyMap()) }

    // Tüm setlerin doluluk kontrolü
    val areAllExercisesCompleted = program.exercises.indices.all { exIndex ->
        val sets = exerciseDetailsMap[exIndex] ?: emptyList()
        val isChecked = completedMap.value[exIndex] == true
        val isFilled = sets.all { it.first.isNotBlank() && it.second.isNotBlank() }
        isChecked && isFilled
    }

    if (isFinishedToday || program.istRestDay) {
        // ... (Tebrikler / Dinlenme Günü Ekranı aynı kalıyor)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (program.istRestDay) Icons.Default.Info else Icons.Default.CheckCircle,
                        contentDescription = null, tint = Color.Black, modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = if (program.istRestDay) "Dinlenme Günü" else "Tebrikler!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (program.istRestDay) "Bugün dinlenme gününüz." else "Bugünün antrenmanını başarıyla tamamladınız.",
                        textAlign = TextAlign.Center, fontSize = 16.sp, color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = onBack, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                        Text("Ana Menüye Dön")
                    }
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") }
                Text(text = program.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            program.exercises.forEachIndexed { exIndex, ex ->
                val previousPR = allRecords.find { it.name == ex.name }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // GEÇMİŞ REKOR / HEDEF BİLGİSİ
                        if (previousPR != null) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.07f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Geçen Haftaki Başarın:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "Setler: ${previousPR.setDetails} (Max: ${previousPR.maxKg}kg)",
                                        fontSize = 12.sp, color = Color.DarkGray
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(text = ex.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = "Hedef: ${ex.sets} Set x ${ex.reps} Tekrar (RIR: ${ex.rir})", fontSize = 14.sp, color = Color.Gray)
                            }
                            Checkbox(
                                checked = completedMap.value[exIndex] ?: false,
                                onCheckedChange = { isChecked ->
                                    val newMap = completedMap.value.toMutableMap()
                                    newMap[exIndex] = isChecked
                                    completedMap.value = newMap
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color.Black)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                        // SET GİRİŞ ALANLARI
                        val setList = exerciseDetailsMap[exIndex] ?: emptyList()
                        setList.forEachIndexed { setIndex, setPair ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("${setIndex + 1}. Set", modifier = Modifier.width(45.dp), fontSize = 13.sp, fontWeight = FontWeight.Medium)

                                OutlinedTextField(
                                    value = setPair.first,
                                    onValueChange = { weight ->
                                        val newList = setList.toMutableList()
                                        newList[setIndex] = weight to setPair.second
                                        exerciseDetailsMap[exIndex] = newList
                                    },
                                    label = { Text("kg") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = getTextFieldColors()
                                )

                                OutlinedTextField(
                                    value = setPair.second,
                                    onValueChange = { reps ->
                                        val newList = setList.toMutableList()
                                        newList[setIndex] = setPair.first to reps
                                        exerciseDetailsMap[exIndex] = newList
                                    },
                                    label = { Text("tekrar") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = getTextFieldColors()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                    val newPRs = mutableListOf<PersonelRecord>()

                    exerciseDetailsMap.forEach { (exIndex, setList) ->
                        if (completedMap.value[exIndex] == true) {
                            // Detayları birleştir: "80x10, 80x8, 75x8"
                            val detailString = setList.joinToString(", ") { "${it.first}x${it.second}" }
                            // Max kiloyu bul
                            val maxKg = setList.mapNotNull { it.first.toDoubleOrNull() }.maxOrNull() ?: 0.0

                            newPRs.add(
                                PersonelRecord(
                                    name = program.exercises[exIndex].name,
                                    maxKg = maxKg.toString(),
                                    setDetails = detailString,
                                    date = today,
                                    sets = program.exercises[exIndex].sets,
                                    reps = program.exercises[exIndex].reps
                                )
                            )
                        }
                    }

                    onFinish(newPRs)
                    isFinishedToday = true
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = areAllExercisesCompleted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
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