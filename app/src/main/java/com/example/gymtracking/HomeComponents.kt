package com.example.gymtracking

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import java.util.Calendar
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun getTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.Black,
    unfocusedBorderColor = Color.Black.copy(alpha = 0.5f),
    focusedLabelColor = Color.Black,
    cursorColor = Color.Black
)

@Composable
fun MacroSummaryCard(macros: UserMacros?) {
    if (macros == null) return // Veri yoksa hiç çizme

    Text(
        text = "Beslenme Hedefleri",
        fontSize = 18.sp,        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Günlük Hedef", fontWeight = FontWeight.Bold)
                Text(macros.date, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutritionItem("Kalori", "${macros.dailyCalories}", "kcal")
                NutritionItem("Protein", "${macros.protein}", "g")
                NutritionItem("Karb", "${macros.carbs}", "g")
                NutritionItem("Yağ", "${macros.fats}", "g")
            }
        }
    }
}

@Composable
fun NutritionItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Text(unit, fontSize = 10.sp, color = Color.Gray)
    }
}
@Composable
fun MotivationCard() {

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
                Text(
                    text = if (lastWeight.isBlank() || lastWeight == "0" || lastWeight == "0.0") {
                        "Yok"
                    } else {
                        "$lastWeight kg"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
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
                Text(
                    text = if (totalPRs == 0) "Yok" else "$totalPRs kg",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
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
fun LastProgressPreview(lastPhotoUri: String?) {
    // Fotoğrafın görünürlük durumu
    var isVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text("Son Gelişim Fotoğrafın", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp) // ProgressScreen ile uyumlu yükseklik
                .padding(top = 8.dp)
                .clickable { isVisible = !isVisible }, // Tıklayınca aç/kapat
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (lastPhotoUri != null) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(if (lastPhotoUri != null) 6.dp else 0.dp)
        ) {
            if (lastPhotoUri != null) {
                Box(modifier = Modifier.fillMaxSize()) {

                    // 1. KATMAN: Fotoğraf (Bulanıklık Efekti ile)
                    AsyncImage(
                        model = lastPhotoUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                // Android 12+ için gerçek buğulama
                                if (!isVisible && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                        35f, 35f, android.graphics.Shader.TileMode.CLAMP
                                    ).asComposeRenderEffect()
                                }
                            },
                        contentScale = ContentScale.Fit
                    )

                    // 2. KATMAN: Gizlilik Overlay (Sadece gizliyken görünür)
                    if (!isVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp).graphicsLayer(rotationZ = 45f) // X işareti
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Görmek için tıkla",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else {
                // Fotoğraf yoksa gösterilecek boş alan
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_workout),
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Henüz fotoğraf eklenmedi", color = Color.Gray)
                    }
                }
            }
        }
    }
}


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
fun RecordCard(record: PersonelRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.1f)),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = record.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = record.date, fontSize = 12.sp, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.maxKg} kg",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                // Set ve Tekrar detayları (Eğer setDetails alanını eklediysen)
                if (record.setDetails.isNotBlank()) {
                    Text(
                        text = record.setDetails,
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}