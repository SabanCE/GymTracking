package com.example.gymtracking
import androidx.room.*
import java.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// 1. PROGRAM TABLOSU
@Entity(tableName = "workout_programs")
data class WorkoutProgram(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val exercises: List<Exercise>, // TypeConverter ile saklanacak
    val istRestDay: Boolean = false
)

// 2. REKOR TABLOSU
@Entity(tableName = "personal_records")
data class PersonelRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val maxKg: String,
    val date: String,
    val sets: String = "",
    val reps: String = "",
    val setDetails: String = "",
    val rir: String = ""
)

@Entity(tableName = "progress_photos")
data class ProgressPhoto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUri: String, // Fotoğrafın dosya yolu/URI'si
    val date: String      // Kayıt tarihi
)

// 3. VÜCUT ÖLÇÜM TABLOSU
@Entity(tableName = "body_measurements")
data class BodyMeasurement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weight: String,
    val bodyFat: String,
    val date: String
)
// 4. KULLANICI MAKRO TABLOSU
@Entity(tableName = "user_macros")
data class UserMacros(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weight: Double,
    val height: Double,
    val age: Int,
    val fatPercentage: Double?,
    val activityLevel: String, // "Sedanter", "Hafif", "Orta", "Ağır"
    val dailyCalories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val date: String
)

// BU BİR TABLO DEĞİL, YARDIMCI SINIF
data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var sets: String = "",
    var reps: String = "",
    var rir: String = ""

)


// HATA BURADAYDI: UserProfile eksik olabilir
data class UserProfile(
    val weight: String = "",
    val bodyFat: String = "",
    val startDay: Int = 1
)



// Antrenman sırasında UI yönetimi için
data class Hareketler(
    val displayName: String,
    val originalName: String,
    val done: androidx.compose.runtime.MutableState<Boolean> = androidx.compose.runtime.mutableStateOf(false),
    val maxKg: androidx.compose.runtime.MutableState<String> = androidx.compose.runtime.mutableStateOf("")
)

class Converters {
    @TypeConverter
    fun fromExerciseList(value: List<Exercise>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toExerciseList(value: String): List<Exercise> {
        val listType = object : TypeToken<List<Exercise>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
