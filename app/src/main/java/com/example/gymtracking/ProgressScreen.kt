package com.example.gymtracking

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.shape.CircleShape
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
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
    var selectedPhotoForZoom by remember { mutableStateOf<ProgressPhoto?>(null) }

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
        Text(text = "Kişisel Rekorlar (PR)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))// Eğer hiç rekor yoksa mesaj göster
        if (records.isEmpty()) {
            Text("Henüz bir rekor kaydedilmedi.", color = Color.Gray, fontStyle = FontStyle.Italic)
        } else {
            // Kategorilere göre rekorları gruplandırıyoruz
            ExerciseLibrary.categories.forEach { (categoryName, exerciseList) ->
                // Bu kategoride en az bir rekor var mı kontrol et
                val categoryRecords = records.filter { it.name in exerciseList }

                if (categoryRecords.isNotEmpty()) {
                    // Kategori Başlığı
                    Text(
                        text = categoryName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    // O kategoriye ait rekor kartları
                    categoryRecords.forEach { record ->
                        RecordCard(record = record)
                    }
                }
            }

            // Eğer kategorize edilemeyen (özel eklenen) hareketler varsa onları da "Diğer" altında gösterelim
            val categorizedNames = ExerciseLibrary.categories.values.flatten()
            val otherRecords = records.filter { it.name !in categorizedNames }

            if (otherRecords.isNotEmpty()) {
                Text(
                    text = "Diğer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                otherRecords.forEach { record ->
                    RecordCard(record = record)
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

            // ... LazyRow içindeki items(photos) bloğu
            items(photos) { photo ->
                var isVisible by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .size(width = 150.dp, height = 200.dp)
                        .combinedClickable( // clickable yerine combinedClickable kullanıyoruz
                            onClick = { isVisible = !isVisible },
                            onLongClick = { selectedPhotoForZoom = photo } // Uzun basınca büyüt
                        ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )

                    {
                    // Tüm katmanları üst üste bindirmek için tek bir Box
                    Box(modifier = Modifier.fillMaxSize()) {

                        // 1. KATMAN: Fotoğraf
                        AsyncImage(
                            model = photo.imageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    // Android 12+ için gerçek buğulama, altı için sadece karartma
                                    if (!isVisible && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                        renderEffect =
                                            android.graphics.RenderEffect.createBlurEffect(
                                                35f, 35f, android.graphics.Shader.TileMode.CLAMP
                                            ).asComposeRenderEffect()
                                    }
                                },
                            contentScale = ContentScale.Crop
                        )

                        // 2. KATMAN: Gizlilik Overlay (Sadece gizliyken görünür)
                        if (!isVisible) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.7f)), // Fotoğrafı tamamen gizlemek için opaklığı artırdık
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        // VisibilityOff yerine garantili çalışan bir ikon yolu
                                        imageVector =Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.graphicsLayer(rotationZ = 45f) // İkonu çarpı (X) yapar
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Gizli",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // 3. KATMAN: Tarih Bandı (Her zaman en üstte ve fotoğrafın üzerinde)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = photo.date,
                                color = Color.White,
                                fontSize = 11.sp
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
    if (selectedPhotoForZoom != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedPhotoForZoom = null },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false // Tam ekran olması için
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Büyük Fotoğraf
                AsyncImage(
                    model = selectedPhotoForZoom?.imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )

                // Üst Bar (Geri ve Sil Butonları)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Geri Dön Butonu
                    androidx.compose.material3.IconButton(
                        onClick = { selectedPhotoForZoom = null },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }

                    // Sil Butonu
                    androidx.compose.material3.IconButton(
                        onClick = {
                            selectedPhotoForZoom?.let { photo ->
                                onDeletePhoto(photo) // MainActivity'deki silme fonksiyonunu çağırır
                                selectedPhotoForZoom = null // Ekranı kapat
                            }
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = Color.Red
                        )
                    }
                }

                // Alt Kısım Tarih Bilgisi
                Text(
                    text = selectedPhotoForZoom?.date ?: "",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

