package com.jvigil.hoofmode.data.repository

import androidx.room.withTransaction
import com.jvigil.hoofmode.data.local.HoofModeDatabase
import com.jvigil.hoofmode.data.local.dao.ActivityLogDao
import com.jvigil.hoofmode.data.local.dao.ScheduleDao
import com.jvigil.hoofmode.data.local.dao.ScheduleItemRow
import com.jvigil.hoofmode.data.local.entity.ActivityLogEntity
import com.jvigil.hoofmode.data.local.entity.ActivityType
import com.jvigil.hoofmode.data.local.entity.ScheduleEntity
import com.jvigil.hoofmode.data.local.entity.ScheduleItemEntity
import com.jvigil.hoofmode.data.local.entity.ScheduleItemType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class ActiveScheduleState(
    val schedule: ScheduleEntity,
    val items: List<ScheduleItemRow>,
    val currentItem: ScheduleItemRow?,
)

@Singleton
class ScheduleRepository @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val activityLogDao: ActivityLogDao,
    private val sessionRepository: SessionRepository,
    private val db: HoofModeDatabase,
) {
    fun observeAll(): Flow<List<ScheduleEntity>> = scheduleDao.observeAll()

    fun observeItemsForSchedule(scheduleId: Long): Flow<List<ScheduleItemRow>> =
        scheduleDao.observeItemsForSchedule(scheduleId)

    /** The active schedule, its full sequence, and whichever item the pointer currently resolves to. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeActiveScheduleState(): Flow<ActiveScheduleState?> =
        scheduleDao.observeActive().flatMapLatest { schedule ->
            if (schedule == null) {
                flowOf(null)
            } else {
                scheduleDao.observeItemsForSchedule(schedule.id).combine(flowOf(schedule)) { items, s ->
                    val current = items.firstOrNull { it.id == s.currentPointerItemId } ?: items.minByOrNull { it.orderIndex }
                    ActiveScheduleState(s, items, current)
                }
            }
        }

    suspend fun getById(id: Long): ScheduleEntity? = scheduleDao.getById(id)

    suspend fun getItemById(itemId: Long): ScheduleItemEntity? = scheduleDao.getItemById(itemId)

    suspend fun createSchedule(name: String): Long = scheduleDao.insert(ScheduleEntity(name = name))

    suspend fun renameSchedule(schedule: ScheduleEntity, name: String) = scheduleDao.update(schedule.copy(name = name))

    suspend fun deleteSchedule(schedule: ScheduleEntity) = scheduleDao.delete(schedule)

    /** Only one schedule drives progression; activating replaces the pointer, history stays intact. */
    suspend fun activateSchedule(scheduleId: Long) {
        db.withTransaction {
            scheduleDao.setActiveSchedule(scheduleId)
            val schedule = scheduleDao.getById(scheduleId) ?: return@withTransaction
            if (schedule.currentPointerItemId == null) {
                val firstItem = scheduleDao.getItemsForSchedule(scheduleId).minByOrNull { it.orderIndex }
                scheduleDao.setPointer(scheduleId, firstItem?.id)
            }
        }
    }

    suspend fun addItem(scheduleId: Long, type: ScheduleItemType, workoutTemplateId: Long?): Long {
        val nextOrder = scheduleDao.maxOrderIndex(scheduleId) + 1
        return scheduleDao.insertItem(
            ScheduleItemEntity(scheduleId = scheduleId, orderIndex = nextOrder, type = type, workoutTemplateId = workoutTemplateId),
        )
    }

    suspend fun removeItem(itemId: Long) = scheduleDao.deleteItem(itemId)

    suspend fun reorderItems(scheduleId: Long, orderedItemIds: List<Long>) {
        orderedItemIds.forEachIndexed { index, id ->
            scheduleDao.getItemById(id)?.let { scheduleDao.updateItem(it.copy(orderIndex = index)) }
        }
    }

    /**
     * The single mutation point for progression (2.7-2.12): resolves whatever the pointer
     * currently points at, records its completion, and advances — looping back to the start
     * once the sequence is exhausted.
     */
    suspend fun completeCurrentItem(scheduleId: Long) {
        db.withTransaction {
            val schedule = scheduleDao.getById(scheduleId) ?: return@withTransaction
            val items = scheduleDao.getItemsForSchedule(scheduleId)
            if (items.isEmpty()) return@withTransaction
            val current = items.firstOrNull { it.id == schedule.currentPointerItemId } ?: items.minBy { it.orderIndex }

            when (current.type) {
                ScheduleItemType.WORKOUT -> sessionRepository.completeWorkoutForScheduleItem(current.id)
                ScheduleItemType.REST -> activityLogDao.insert(
                    ActivityLogEntity(date = LocalDate.now(), type = ActivityType.REST, sessionId = null, scheduleItemId = current.id),
                )
            }

            val sorted = items.sortedBy { it.orderIndex }
            val currentPos = sorted.indexOfFirst { it.id == current.id }
            val nextItem = sorted[(currentPos + 1) % sorted.size]
            val completionType = if (current.type == ScheduleItemType.WORKOUT) ActivityType.WORKOUT else ActivityType.REST
            scheduleDao.update(
                schedule.copy(
                    currentPointerItemId = nextItem.id,
                    lastCompletionDate = LocalDate.now(),
                    lastCompletionType = completionType,
                ),
            )
        }
    }
}
