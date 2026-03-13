package com.example.gymtracking
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateProgramScreen(onSave: (WorkoutProgram) -> Unit, onBack: () -> Unit,existingProgram: WorkoutProgram? = null) {
    var programName by remember { mutableStateOf(existingProgram?.name ?: "") }
    var isRestDay by remember { mutableStateOf(existingProgram?.istRestDay ?: false) }
    val exercises = remember {
        mutableStateListOf<Exercise>().apply {
            if (existingProgram != null) {
                addAll(existingProgram.exercises)
            }
        }
        }

        // Diyalog için durumlar
    var showExerciseDialog by remember { mutableStateOf(false) }
    var activeExerciseIndex by remember { mutableIntStateOf(-1) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)
        .verticalScroll(rememberScrollState())) {

        Spacer(modifier = Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") }
            Text(text = if(existingProgram != null) "Düzenle" else "Yeni Program", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dinlenme Günü Checkbox
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

        // Program Adı Girişi
        OutlinedTextField(
            value = programName,
            onValueChange = { if (!isRestDay) programName = it },
            label = { Text("Program Adı (Örn: Göğüs & Kol)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRestDay,
            colors = getTextFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )

        if (!isRestDay) {
            Text(text = "Egzersizler", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))

            // DÖNGÜ BAŞLANGICI
            exercises.forEachIndexed { index, ex ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {

                        // ÜST SATIR: Egzersiz Seçimi ve Silme Butonu
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = ex.name,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Egzersiz Seç") },
                                    modifier = Modifier.fillMaxWidth(),
                                    // trailingIcon'u burada tutuyoruz ama Box sayesinde tıklama her yerde aktif
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = Color.Black) },
                                    enabled = true,
                                    colors = getTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable {
                                            activeExerciseIndex = index
                                            showExerciseDialog = true
                                        }
                                )
                            }

                            // Tekil Egzersizi Silme Butonu (X)
                            IconButton(
                                onClick = { exercises.removeAt(index) },
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Egzersizi Sil",
                                    tint = Color.Red.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ALT SATIR: Set, Tekrar ve RIR Girişleri (Tam Hizalı)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = ex.sets,
                                onValueChange = { exercises[index] = ex.copy(sets = it) },
                                label = { Text("Set", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = getTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = ex.reps,
                                onValueChange = { exercises[index] = ex.copy(reps = it) },
                                label = { Text("Tekrar", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = getTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = ex.rir,
                                onValueChange = { exercises[index] = ex.copy(rir = it) },
                                label = { Text("RIR", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = getTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { exercises.add(Exercise("", "", "", "", "")) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Egzersiz Ekle")
            }
        }

        // EGZERSİZ SEÇME DİYALOĞU (Döngü dışında ama Composable içinde)
        if (showExerciseDialog) {
            AlertDialog(
                onDismissRequest = { showExerciseDialog = false },
                title = { Text("Egzersiz Seçin") },
                text = {
                    LazyColumn(modifier = Modifier.height(400.dp)) {
                        ExerciseLibrary.categories.forEach { (category, exerciseList) ->
                            item {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(8.dp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                            items(exerciseList) { exerciseName ->
                                Text(
                                    text = exerciseName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (activeExerciseIndex != -1) {
                                                exercises[activeExerciseIndex] =
                                                    exercises[activeExerciseIndex].copy(name = exerciseName)
                                            }
                                            showExerciseDialog = false
                                        }
                                        .padding(16.dp)
                                )
                                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.4f))
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showExerciseDialog = false }) { Text("Kapat", color = Color.Black) } }
            )
        }

        // Kaydet Butonu Mantığı
        val isFormValid = if (isRestDay) {
            programName.isNotBlank()
        } else {
            programName.isNotBlank() &&
                    exercises.isNotEmpty() &&
                    exercises.all { it.name.isNotBlank() && it.sets.isNotBlank() && it.reps.isNotBlank() && it.rir.isNotBlank()  }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (isFormValid) {
                    onSave(WorkoutProgram(
                        id = existingProgram?.id ?: 0,
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
            Text(if(existingProgram != null) "Programı Güncelle" else "Programı Kaydet")
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}


