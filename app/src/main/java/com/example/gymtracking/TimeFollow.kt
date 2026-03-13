package com.example.gymtracking

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Zaman yönetimi için yardımcı fonksiyonlar
object WorkoutPrefs {
    private const val PREFS_NAME = "workout_prefs"
    private const val KEY_CURRENT_INDEX = "current_program_index"
    private const val KEY_LAST_COMPLETED_DATE = "last_completion_date"
    private const val KEY_TOTAL_DAYS = "total_days_count"
    private const val KEY_LAST_SHIFT_DATE = "last_shift_date"

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getTotalDaysCount(context: Context): Int {
        return getPrefs(context).getInt(KEY_TOTAL_DAYS, 1)
    }

    fun getNextProgramIndex(context: Context, totalPrograms: Int): Int {
        if (totalPrograms == 0) return 0
        val prefs = getPrefs(context)
        var currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)
        // Eğer program silindiyse ve index dışarıda kaldıysa sıfırla
        if (currentIndex >= totalPrograms) {
            currentIndex = 0
            prefs.edit().putInt(KEY_CURRENT_INDEX, 0).apply()
        }
        return currentIndex
    }

    // Antrenman bittiğinde BUGÜNÜN tarihini kaydet
    fun completeWorkout(context: Context, totalPrograms: Int) {
        val prefs = getPrefs(context)
        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

        prefs.edit()
            .putString(KEY_LAST_COMPLETED_DATE, today)
            .apply()
    }

    // Gelişmiş gün kaydırma mantığı
    fun checkAndShiftDay(context: Context, totalPrograms: Int) {
        if (totalPrograms == 0) return
        val prefs = getPrefs(context)
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        
        val todayStr = sdf.format(Date())
        val lastShiftDateStr = prefs.getString(KEY_LAST_SHIFT_DATE, "") ?: ""

        // Eğer bugün henüz gün kaydırma kontrolü yapılmadıysa (Tarih dünden farklıysa)
        if (lastShiftDateStr != todayStr) {
            if (lastShiftDateStr.isEmpty()) {
                // Uygulama ilk kez açılıyor, bugünü başlangıç olarak kaydet ve çık
                prefs.edit().putString(KEY_LAST_SHIFT_DATE, todayStr).apply()
                return
            }

            var daysToShift = 1
            try {
                val lastShiftDate = sdf.parse(lastShiftDateStr)
                val todayDate = sdf.parse(todayStr)

                if (lastShiftDate != null && todayDate != null) {
                    val diffInMillis = todayDate.time - lastShiftDate.time
                    val calculatedDays = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
                    // Eğer milisaniye farkı 1 tam günden az olsa bile (örneğin 23 saat) 
                    // tarih değiştiği için en az 1 gün kaydırıyoruz
                    if (calculatedDays > 1) {
                        daysToShift = calculatedDays
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)
            val currentTotalDays = prefs.getInt(KEY_TOTAL_DAYS, 1)

            // Program sırasını ve toplam gün sayısını aradaki fark kadar ileri taşı
            val nextIndex = (currentIndex + daysToShift) % totalPrograms
            
            prefs.edit()
                .putInt(KEY_CURRENT_INDEX, nextIndex)
                .putInt(KEY_TOTAL_DAYS, currentTotalDays + daysToShift)
                .putString(KEY_LAST_SHIFT_DATE, todayStr)
                .apply()
        }
    }

    fun isWorkoutFinishedToday(context: Context): Boolean {
        val prefs = getPrefs(context)
        val lastDate = prefs.getString(KEY_LAST_COMPLETED_DATE, "") ?: ""
        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        return lastDate == today
    }
}
