package com.jvigil.hoofmode.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jvigil.hoofmode.data.local.entity.ScheduleEntity
import com.jvigil.hoofmode.data.local.entity.ScheduleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules ORDER BY name ASC")
    fun observeAll(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<ScheduleEntity?>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getById(id: Long): ScheduleEntity?

    @Insert
    suspend fun insert(schedule: ScheduleEntity): Long

    @Update
    suspend fun update(schedule: ScheduleEntity)

    @Delete
    suspend fun delete(schedule: ScheduleEntity)

    @Query("UPDATE schedules SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAll()

    @Query("UPDATE schedules SET isActive = 1 WHERE id = :id")
    suspend fun activate(id: Long)

    @Transaction
    suspend fun setActiveSchedule(id: Long) {
        deactivateAll()
        activate(id)
    }

    @Query("UPDATE schedules SET currentPointerItemId = :itemId WHERE id = :scheduleId")
    suspend fun setPointer(scheduleId: Long, itemId: Long?)

    // --- Schedule items ---

    @Query(
        """
        SELECT si.id AS id, si.scheduleId AS scheduleId, si.orderIndex AS orderIndex,
               si.type AS type, si.workoutTemplateId AS workoutTemplateId,
               wt.name AS workoutTemplateName
        FROM schedule_items si
        LEFT JOIN workout_templates wt ON wt.id = si.workoutTemplateId
        WHERE si.scheduleId = :scheduleId
        ORDER BY si.orderIndex ASC
        """,
    )
    fun observeItemsForSchedule(scheduleId: Long): Flow<List<ScheduleItemRow>>

    @Query(
        """
        SELECT si.id AS id, si.scheduleId AS scheduleId, si.orderIndex AS orderIndex,
               si.type AS type, si.workoutTemplateId AS workoutTemplateId,
               wt.name AS workoutTemplateName
        FROM schedule_items si
        LEFT JOIN workout_templates wt ON wt.id = si.workoutTemplateId
        WHERE si.scheduleId = :scheduleId
        ORDER BY si.orderIndex ASC
        """,
    )
    suspend fun getItemsForSchedule(scheduleId: Long): List<ScheduleItemRow>

    @Query("SELECT * FROM schedule_items WHERE id = :id")
    suspend fun getItemById(id: Long): ScheduleItemEntity?

    @Insert
    suspend fun insertItem(item: ScheduleItemEntity): Long

    @Update
    suspend fun updateItem(item: ScheduleItemEntity)

    @Update
    suspend fun updateItems(items: List<ScheduleItemEntity>)

    @Query("DELETE FROM schedule_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM schedule_items WHERE scheduleId = :scheduleId")
    suspend fun maxOrderIndex(scheduleId: Long): Int
}
