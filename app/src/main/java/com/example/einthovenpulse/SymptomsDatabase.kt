package com.example.einthovenpulse

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.RoomDatabase

@Dao
interface MeasurementsDao {
    @Insert
    suspend fun submitMeasurements(measurements: Measurements)
}

@Database(entities = [Measurements::class], version = 1)
abstract class SymptomsDatabase: RoomDatabase() {
    abstract fun measurementsDao(): MeasurementsDao
}
