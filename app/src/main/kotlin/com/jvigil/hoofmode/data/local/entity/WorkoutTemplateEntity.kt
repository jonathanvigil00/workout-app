package com.jvigil.hoofmode.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrderMode { UNORDERED, ENFORCED }

@Entity(tableName = "workout_templates")
data class WorkoutTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val orderMode: OrderMode = OrderMode.UNORDERED,
)
