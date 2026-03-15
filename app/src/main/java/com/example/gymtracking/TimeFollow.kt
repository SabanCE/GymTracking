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
    private const val KEY_SKIPPED_INDEX = "skipped_program_index"

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getTotalDaysCount(context: Context): Int {
        return getPrefs(context).getInt(KEY_TOTAL_DAYS, 1)
    }

    fun getNextProgramIndex(context: Context, totalPrograms: Int): Int {
        if (totalPrograms == 0) return 0
        val prefs = getPrefs(context)
        var currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)
        if (currentIndex >= totalPrograms) {
            currentIndex = 0
            prefs.edit().putInt(KEY_CURRENT_INDEX, 0).apply()
        }
        return currentIndex
    }

    fun completeWorkout(context: Context, totalPrograms: Int) {
        val prefs = getPrefs(context)
        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

        prefs.edit()
            .putString(KEY_LAST_COMPLETED_DATE, today)
            .putInt(KEY_SKIPPED_INDEX, -1) // Tamamlandığı için atlanan bilgiyi temizle
            .apply()
    }

    fun checkAndShiftDay(context: Context, totalPrograms: Int, allPrograms: List<WorkoutProgram>) {
        if (totalPrograms == 0) return
        val prefs = getPrefs(context)
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        val todayStr = sdf.format(Date())
        val lastShiftDateStr = prefs.getString(KEY_LAST_SHIFT_DATE, "") ?: ""

        // Sadece tarih dizeleri farklıysa işlem yap (24 saat dolmasa bile gün dönmüşse)
        if (lastShiftDateStr != todayStr) {
            if (lastShiftDateStr.isEmpty()) {
                prefs.edit().putString(KEY_LAST_SHIFT_DATE, todayStr).apply()
                return
            }

            try {
                val lastShiftDate = sdf.parse(lastShiftDateStr)
                val todayDate = sdf.parse(todayStr)

                if (lastShiftDate != null && todayDate != null) {
                    // diffInDays hesabını gün bazında garantilemek için en az 1 gün sayıyoruz
                    val diffInMillis = todayDate.time - lastShiftDate.time
                    var diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()

                    // Eğer tarih değiştiyse ama 24 saat geçmediyse diffInDays 0 gelir, bunu 1'e zorluyoruz
                    if (diffInDays <= 0) diffInDays = 1

                    val lastCompletedDate = prefs.getString(KEY_LAST_COMPLETED_DATE, "") ?: ""
                    val currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)
                    val lastProgram = allPrograms.getOrNull(currentIndex)

                    // Dünkü antrenman yapıldı mı veya dinlenme günü müydü?
                    val wasLastCompleted = lastCompletedDate == lastShiftDateStr
                    val wasRestDay = lastProgram?.istRestDay == true

                    // Eğer antrenman yapılmadıysa ve dinlenme günü değilse, bu indeksi "kaçırılan" olarak işaretle
                    if (!wasLastCompleted && !wasRestDay) {
                        prefs.edit().putInt(KEY_SKIPPED_INDEX, currentIndex).apply()
                    }

                    // PROGRAMI İLERLET (Hedef 1/7 -> 2/7 olması için)
                    // Not: Kaç gün geçtiyse o kadar ilerletiyoruz
                    val nextIndex = (currentIndex + diffInDays) % totalPrograms
                    val currentTotalDays = prefs.getInt(KEY_TOTAL_DAYS, 1)

                    prefs.edit()
                        .putInt(KEY_CURRENT_INDEX, nextIndex)
                        .putInt(KEY_TOTAL_DAYS, currentTotalDays + diffInDays)
                        .putString(KEY_LAST_SHIFT_DATE, todayStr)
                        .apply()
                }
            } catch (e: Exception) {
                // Hata durumunda sadece tarihi güncelle ki sonsuz döngüye girmesin
                prefs.edit().putString(KEY_LAST_SHIFT_DATE, todayStr).apply()
            }
        }
    }

    fun isWorkoutFinishedToday(context: Context): Boolean {
        val prefs = getPrefs(context)
        val lastDate = prefs.getString(KEY_LAST_COMPLETED_DATE, "") ?: ""
        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        return lastDate == today
    }

    fun getPreviousProgramIndex(context: Context, totalPrograms: Int): Int {
        val prefs = getPrefs(context)
        val currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)
        return if (currentIndex == 0) totalPrograms - 1 else currentIndex - 1
    }

    fun forceNextProgram(context: Context, totalPrograms: Int) {
        if (totalPrograms == 0) return
        val prefs = getPrefs(context)
        val currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)
        val nextIndex = (currentIndex + 1) % totalPrograms
        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

        prefs.edit()
            .putInt(KEY_CURRENT_INDEX, nextIndex)
            .putString(KEY_LAST_COMPLETED_DATE, today) // BUGÜNÜ BİTTİ OLARAK İŞARETLE
            .putInt(KEY_SKIPPED_INDEX, -1) // Varsa kaçırılan uyarısını temizle
            .apply()
    }

    // Atlanan bir program olup olmadığını kontrol et
    fun getSkippedProgramIndex(context: Context): Int {
        return getPrefs(context).getInt(KEY_SKIPPED_INDEX, -1)
    }

    // Atlanan programa geri dön
    fun returnToSkippedWorkout(context: Context, skippedIndex: Int) {
        getPrefs(context).edit()
            .putInt(KEY_CURRENT_INDEX, skippedIndex)
            .putInt(KEY_SKIPPED_INDEX, -1) // Geri dönüldüğü için temizle
            .apply()
    }

    fun hasMissedWorkout(context: Context): Boolean {
        return getSkippedProgramIndex(context) != -1
    }
    private const val KEY_LAST_DISMISSED_WARNING_DATE = "last_dismissed_warning_date"

    // Uyarıyı bugün için kapat
    fun dismissWarning(context: Context) {
        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        getPrefs(context).edit().putString(KEY_LAST_DISMISSED_WARNING_DATE, today).apply()
    }

    // Uyarı bugün kapatılmış mı kontrol et
    fun isWarningDismissedToday(context: Context): Boolean {
        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        val lastDismissed = getPrefs(context).getString(KEY_LAST_DISMISSED_WARNING_DATE, "")
        return today == lastDismissed
    }
}

