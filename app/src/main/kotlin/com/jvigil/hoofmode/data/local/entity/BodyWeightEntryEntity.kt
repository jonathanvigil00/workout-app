package com.jvigil.hoofmode.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "body_weight_entries", indices = [Index("date", unique = true)])
data class BodyWeightEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val weight: Double,
    val note: String?,
)
