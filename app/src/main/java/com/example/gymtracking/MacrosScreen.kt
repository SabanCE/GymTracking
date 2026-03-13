package com.example.gymtracking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MacrosScreen(onSave: (UserMacros) -> Unit, onBack: () -> Unit) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var fatPercent by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("Orta (3-4 gün spor)") }

    val activityMultipliers = mapOf(
        "Az (Spor yok)" to 1.2,
        "Hafif (1-2 gün)" to 1.375,
        "Orta (3-4 gün spor)" to 1.55,
        "Ağır (5+ gün spor)" to 1.725
    )

    val isFormValid = weight.isNotBlank() && height.isNotBlank() && age.isNotBlank() && activityLevel.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
            }
            Text(text = "Makro Hesapla", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Kişisel Bilgiler", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))


        // Girdi Alanlarını içeren Gri Arka Planlı Kart
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Kilo (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = getTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Boy (cm)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = getTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Yaş") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = getTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = fatPercent,
                    onValueChange = { fatPercent = it },
                    label = { Text("Yağ Oranı % (Opsiyonel)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = getTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Aktivite Seviyesi Seçimi - Gri Kart İçinde
        Text("Haftalık Aktivite", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                activityMultipliers.keys.forEach { level ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activityLevel = level }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = activityLevel == level,
                            onClick = { activityLevel = level },
                            colors = RadioButtonDefaults.colors(selectedColor = Color.Black)
                        )
                        Text(text = level, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Hesapla Butonu - Siyah Tema
        Button(
            onClick = {
                val w = weight.toDoubleOrNull() ?: 0.0
                val h = height.toDoubleOrNull() ?: 0.0
                val a = age.toIntOrNull() ?: 0
                val multiplier = activityMultipliers[activityLevel] ?: 1.55

                val bmr = (10 * w) + (6.25 * h) - (5 * a) + 5
                val tdee = (bmr * multiplier).toInt()

                val prot = (w * 2.0).toInt()
                val fat = (w * 0.8).toInt()
                val carb = (tdee - (prot * 4 + fat * 9)) / 4

                onSave(UserMacros(
                    weight = w, height = h, age = a,
                    fatPercentage = fatPercent.toDoubleOrNull(),
                    activityLevel = activityLevel,
                    dailyCalories = tdee, protein = prot, carbs = carb, fats = fat,
                    date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                ))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = isFormValid,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Hesapla ve Kaydet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
