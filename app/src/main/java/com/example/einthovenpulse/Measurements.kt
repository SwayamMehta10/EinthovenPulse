package com.example.einthovenpulse

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Measurements(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo val heartRate: Int,
    @ColumnInfo val respiratoryRate: Int,
    @ColumnInfo val nausea: Int = 0,
    @ColumnInfo val headache: Int = 0,
    @ColumnInfo val diarrhea: Int = 0,
    @ColumnInfo val soarThroat: Int = 0,
    @ColumnInfo val fever: Int = 0,
    @ColumnInfo val muscleAche: Int = 0,
    @ColumnInfo val lossOSmellOrTaste: Int = 0,
    @ColumnInfo val cough: Int = 0,
    @ColumnInfo val shortnessOBreath: Int = 0,
    @ColumnInfo val feelingTired: Int = 0,
)
