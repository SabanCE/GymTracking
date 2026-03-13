package com.example.gymtracking

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    // Programlar
    @Query("SELECT * FROM workout_programs")
    fun getAllPrograms(): Flow<List<WorkoutProgram>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: WorkoutProgram)

    @Delete
    suspend fun deleteProgram(program: WorkoutProgram)

    @Update
    suspend fun updateProgram(program: WorkoutProgram)

    // Rekorlar
    @Query("SELECT * FROM personal_records")
    fun getAllRecords(): Flow<List<PersonelRecord>>

    @Query("SELECT * FROM personal_records")
    suspend fun getAllRecordsOnce(): List<PersonelRecord>

    @Query("DELETE FROM personal_records WHERE name = :exerciseName")
    suspend fun deleteRecordByName(exerciseName: String)

    @Transaction
    suspend fun insertOrUpdateRecord(record: PersonelRecord) {
        deleteRecordByName(record.name)
        insertRecord(record)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PersonelRecord)

    // Ölçümler
    @Query("SELECT * FROM body_measurements")
    fun getAllMeasurements(): Flow<List<BodyMeasurement>>

    @Insert
    suspend fun insertMeasurement(measurement: BodyMeasurement)

    // Fotoğraflar
    @Query("SELECT * FROM progress_photos")
    fun getAllPhotos(): Flow<List<ProgressPhoto>>

    @Insert
    suspend fun insertPhoto(photo: ProgressPhoto)

    @Delete
    suspend fun deletePhoto(photo: ProgressPhoto)

    // WorkoutDao.kt içine eklenecekler:
    @Query("SELECT * FROM user_macros ORDER BY id DESC LIMIT 1")
    fun getLastMacros(): Flow<UserMacros?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacros(macros: UserMacros)
}
