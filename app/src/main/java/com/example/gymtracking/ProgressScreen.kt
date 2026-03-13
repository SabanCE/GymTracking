package com.example.gymtracking

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    onMeasurementSave: (BodyMeasurement) -> Unit,
    contentAfterPhotos: @Composable () -> Unit = {}
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
        Spacer(modifier = Modifier.height(12.dp))
        contentAfterPhotos()
        Spacer(modifier = Modifier.height(40.dp))
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = getTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newFat,
                        onValueChange = { newFat = it },
                        label = { Text("Yağ Oranı (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = getTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
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
