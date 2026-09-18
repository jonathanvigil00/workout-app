package com.jvigil.hoofmode.data.repository

import androidx.room.withTransaction
import com.jvigil.hoofmode.data.local.HoofModeDatabase
import com.jvigil.hoofmode.data.local.dao.ActivityLogDao
import com.jvigil.hoofmode.data.local.dao.BestSetRow
import com.jvigil.hoofmode.data.local.dao.ScheduleDao
import com.jvigil.hoofmode.data.local.dao.SessionExerciseDao
import com.jvigil.hoofmode.data.local.dao.SessionExerciseRow
import com.jvigil.hoofmode.data.local.dao.SetEntryDao
import com.jvigil.hoofmode.data.local.dao.WorkoutSessionDao
import com.jvigil.hoofmode.data.local.dao.WorkoutTemplateDao
import com.jvigil.hoofmode.data.local.entity.ActivityLogEntity
import com.jvigil.hoofmode.data.local.entity.ActivityType
import com.jvigil.hoofmode.data.local.entity.ScheduleItemType
import com.jvigil.hoofmode.data.local.entity.SessionExerciseEntity
import com.jvigil.hoofmode.data.local.entity.SetEntryEntity
import com.jvigil.hoofmode.data.local.entity.WorkoutSessionEntity
import com.jvigil.hoofmode.domain.prdetection.PrDetector
import com.jvigil.hoofmode.domain.prdetection.PrResult
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: WorkoutSessionDao,
    private val sessionExerciseDao: SessionExerciseDao,
    private val setEntryDao: SetEntryDao,
    private val activityLogDao: ActivityLogDao,
    private val scheduleDao: ScheduleDao,
    private val workoutTemplateDao: WorkoutTemplateDao,
    private val db: HoofModeDatabase,
) {
    fun observeAllSessions(): Flow<List<WorkoutSessionEntity>> = sessionDao.observeAll()

    fun observeSession(sessionId: Long): Flow<WorkoutSessionEntity?> = sessionDao.observeById(sessionId)

    suspend fun getSession(sessionId: Long): WorkoutSessionEntity? = sessionDao.getById(sessionId)

    fun observeSessionExercises(sessionId: Long): Flow<List<SessionExerciseRow>> =
        sessionExerciseDao.observeForSession(sessionId)

    fun observeSets(sessionExerciseId: Long): Flow<List<SetEntryEntity>> =
        setEntryDao.observeForSessionExercise(sessionExerciseId)

    fun observeBestSet(exerciseId: Long): Flow<BestSetRow?> = setEntryDao.observeBestSet(exerciseId)

    fun observeAllBestSets(): Flow<List<BestSetRow>> = setEntryDao.observeAllBestSets()

    fun observeWeightProgression(exerciseId: Long, from: Instant) = setEntryDao.observeWeightProgression(exerciseId, from)

    fun observeWeeklyVolume(from: Instant) = setEntryDao.observeWeeklyVolume(from)

    suspend fun startAdHocSession(): Long =
        sessionDao.insert(
            WorkoutSessionEntity(startTime = Instant.now(), endTime = null, notes = null, sourceScheduleItemId = null, isAdHoc = true),
        )

    /**
     * Reuses an already-open session for this schedule slot, or starts a fresh one pre-populated
     * with the source workout template's exercises (in template order, no sets yet) — otherwise
     * a scheduled workout would open empty and the template would be pointless. If a session is
     * being resumed but has no exercises yet (nothing logged against it either), it's backfilled
     * from the template too — covers both a stale pre-fix session and a template edited after
     * the slot was first opened.
     */
    suspend fun getOrStartSessionForScheduleItem(scheduleItemId: Long): Long = db.withTransaction {
        val existing = sessionDao.getOpenSessionForScheduleItem(scheduleItemId)
        val sessionId = existing?.id ?: sessionDao.insert(
            WorkoutSessionEntity(startTime = Instant.now(), endTime = null, notes = null, sourceScheduleItemId = scheduleItemId, isAdHoc = false),
        )
        if (sessionExerciseDao.getForSession(sessionId).isEmpty()) {
            val item = scheduleDao.getItemById(scheduleItemId)
            if (item?.type == ScheduleItemType.WORKOUT && item.workoutTemplateId != null) {
                workoutTemplateDao.getExercisesForTemplate(item.workoutTemplateId).forEach { templateExercise ->
                    sessionExerciseDao.insert(
                        SessionExerciseEntity(
                            sessionId = sessionId,
                            exerciseId = templateExercise.exerciseId,
                            orderIndex = templateExercise.orderIndex,
                        ),
                    )
                }
            }
        }
        sessionId
    }

    suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long): Long {
        val nextOrder = sessionExerciseDao.maxOrderIndex(sessionId) + 1
        return sessionExerciseDao.insert(SessionExerciseEntity(sessionId = sessionId, exerciseId = exerciseId, orderIndex = nextOrder))
    }

    suspend fun removeExerciseFromSession(sessionExerciseId: Long) = sessionExerciseDao.delete(sessionExerciseId)

    suspend fun reorderSessionExercises(orderedSessionExerciseIds: List<Long>) {
        orderedSessionExerciseIds.forEachIndexed { index, id ->
            sessionExerciseDao.getById(id)?.let { sessionExerciseDao.update(it.copy(orderIndex = index)) }
        }
    }

    suspend fun setExerciseMarkedDone(sessionExerciseId: Long, done: Boolean) {
        sessionExerciseDao.getById(sessionExerciseId)?.let { sessionExerciseDao.update(it.copy(isMarkedDone = done)) }
    }

    suspend fun logSet(sessionExerciseId: Long, weight: Double, reps: Int, rpe: Int?, note: String?): PrResult {
        val sessionExercise = sessionExerciseDao.getById(sessionExerciseId) ?: return PrResult.NONE
        val priorBest = setEntryDao.getMaxWeight(sessionExercise.exerciseId)
        val prResult = PrDetector.evaluate(weight, priorBest)
        val setNumber = setEntryDao.maxSetNumber(sessionExerciseId) + 1
        setEntryDao.insert(
            SetEntryEntity(
                sessionExerciseId = sessionExerciseId,
                setNumber = setNumber,
                weight = weight,
                reps = reps,
                rpe = rpe,
                note = note,
                isPR = prResult == PrResult.NEW_PR,
            ),
        )
        return prResult
    }

    suspend fun updateSet(set: SetEntryEntity) {
        val sessionExercise = sessionExerciseDao.getById(set.sessionExerciseId) ?: return
        val priorBest = setEntryDao.getMaxWeightExcluding(sessionExercise.exerciseId, set.id)
        val prResult = PrDetector.evaluate(set.weight, priorBest)
        setEntryDao.update(set.copy(isPR = prResult == PrResult.NEW_PR))
    }

    suspend fun deleteSet(setId: Long) = setEntryDao.delete(setId)

    suspend fun updateSessionNotes(sessionId: Long, notes: String?) {
        sessionDao.getById(sessionId)?.let { sessionDao.update(it.copy(notes = notes)) }
    }

    suspend fun deleteSession(session: WorkoutSessionEntity) = sessionDao.delete(session)

    /** Ends a session and logs its calendar marker. Safe to call more than once. */
    suspend fun endSession(sessionId: Long) {
        db.withTransaction {
            val session = sessionDao.getById(sessionId) ?: return@withTransaction
            if (session.endTime != null) return@withTransaction
            val ended = session.copy(endTime = Instant.now())
            sessionDao.update(ended)
            activityLogDao.insert(
                ActivityLogEntity(date = LocalDate.now(), type = ActivityType.WORKOUT, sessionId = sessionId, scheduleItemId = ended.sourceScheduleItemId),
            )
        }
    }

    /** "Complete Workout" from the schedule — starts a session first if none was ever opened (2.10). */
    suspend fun completeWorkoutForScheduleItem(scheduleItemId: Long) {
        db.withTransaction {
            val sessionId = getOrStartSessionForScheduleItem(scheduleItemId)
            endSession(sessionId)
        }
    }

    /** Quick-repeat: a fresh ad-hoc session with the same exercises, no copied sets — independent of the schedule pointer. */
    suspend fun repeatSession(sourceSessionId: Long): Long {
        val exercises = sessionExerciseDao.getForSession(sourceSessionId)
        val newSessionId = startAdHocSession()
        exercises.forEach { row ->
            sessionExerciseDao.insert(SessionExerciseEntity(sessionId = newSessionId, exerciseId = row.exerciseId, orderIndex = row.orderIndex))
        }
        return newSessionId
    }
}
