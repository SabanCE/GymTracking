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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProgramsScreen(
    programs: List<WorkoutProgram>,
    onAddNew: () -> Unit,
    onDelete: (WorkoutProgram) -> Unit,
    onBack: () -> Unit,
    onEdit: (WorkoutProgram) -> Unit
) {
    val context = LocalContext.current
    // Dialog durumlarını takip eden state'ler
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSetTodayDialog by remember { mutableStateOf(false) }
    var programToDelete by remember { mutableStateOf<WorkoutProgram?>(null) }
    var programToSetToday by remember { mutableStateOf<Pair<Int, WorkoutProgram>?>(null) }

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
                itemsIndexed(programs) { index, program ->
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

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // BUGÜNÜN PROGRAMI YAP BUTONU
                                    IconButton(onClick = {
                                        programToSetToday = index to program
                                        showSetTodayDialog = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Bugünün Programı Yap",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    // DÜZENLEME BUTONU
                                    IconButton(onClick = { onEdit(program) }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Düzenle", tint = Color.Black)
                                    }

                                    // SİLME BUTONU
                                    IconButton(onClick = {
                                        programToDelete = program
                                        showDeleteDialog = true
                                    }) {
                                        Icon(Icons.Default.Delete, "Sil", tint = Color.Red.copy(alpha = 0.7f))
                                    }

                                    if (!program.istRestDay) {
                                        IconButton(onClick = { isExpanded = !isExpanded }) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                                                tint = Color.Gray
                                            )
                                        }
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
                                        Text(text = "${ex.sets}x${ex.reps} (RIR: ${ex.rir})", fontSize = 14.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // BUGÜNÜN PROGRAMI ONAY ALERTI
    if (showSetTodayDialog && programToSetToday != null) {
        AlertDialog(
            onDismissRequest = { showSetTodayDialog = false },
            title = { Text(text = "Programı Değiştir", fontWeight = FontWeight.Bold) },
            text = { Text(text = "${programToSetToday?.second?.name} programını bugünün programı olarak ayarlamak istediğinizden emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        programToSetToday?.let { (index, _) ->
                            WorkoutPrefs.setCurrentProgramIndex(context, index)
                        }
                        showSetTodayDialog = false
                        programToSetToday = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Evet, Ayarla", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSetTodayDialog = false
                    programToSetToday = null
                }) {
                    Text("İptal", color = Color.Black)
                }
            }
        )
    }

    // SİLME ONAY ALERTI
    if (showDeleteDialog && programToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Programı Sil", fontWeight = FontWeight.Bold) },
            text = { Text(text = "${programToDelete?.name} programını silmek istediğinizden emin misiniz?") },
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
