package com.jvigil.hoofmode.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * The pointer is stored as a reference to the current [ScheduleItemEntity], not a raw list
 * index — indices shift when items are inserted/removed/reordered, but an id reference stays
 * valid (or cleanly nulls out via ON DELETE SET NULL) regardless of edits to the sequence.
 *
 * [lastCompletionDate]/[lastCompletionType] record the most recent time this schedule advanced.
 * The Home screen uses them to show a "completed" state for the rest of that calendar day
 * instead of immediately revealing the newly-advanced pointer item — the next item only surfaces
 * once the date has rolled over.
 */
@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["currentPointerItemId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("currentPointerItemId")],
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isActive: Boolean = false,
    val currentPointerItemId: Long? = null,
    val lastCompletionDate: LocalDate? = null,
    val lastCompletionType: ActivityType? = null,
)
