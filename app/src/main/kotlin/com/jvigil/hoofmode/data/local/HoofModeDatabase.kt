package com.jvigil.hoofmode.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jvigil.hoofmode.data.local.dao.ActivityLogDao
import com.jvigil.hoofmode.data.local.dao.BodyWeightEntryDao
import com.jvigil.hoofmode.data.local.dao.ExerciseDao
import com.jvigil.hoofmode.data.local.dao.ProgressPhotoDao
import com.jvigil.hoofmode.data.local.dao.ScheduleDao
import com.jvigil.hoofmode.data.local.dao.SessionExerciseDao
import com.jvigil.hoofmode.data.local.dao.SetEntryDao
import com.jvigil.hoofmode.data.local.dao.WorkoutSessionDao
import com.jvigil.hoofmode.data.local.dao.WorkoutTemplateDao
import com.jvigil.hoofmode.data.local.entity.ActivityLogEntity
import com.jvigil.hoofmode.data.local.entity.BodyWeightEntryEntity
import com.jvigil.hoofmode.data.local.entity.ExerciseEntity
import com.jvigil.hoofmode.data.local.entity.ProgressPhotoEntity
import com.jvigil.hoofmode.data.local.entity.ScheduleEntity
import com.jvigil.hoofmode.data.local.entity.ScheduleItemEntity
import com.jvigil.hoofmode.data.local.entity.SessionExerciseEntity
import com.jvigil.hoofmode.data.local.entity.SetEntryEntity
import com.jvigil.hoofmode.data.local.entity.WorkoutSessionEntity
import com.jvigil.hoofmode.data.local.entity.WorkoutTemplateEntity
import com.jvigil.hoofmode.data.local.entity.WorkoutTemplateExerciseEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutTemplateEntity::class,
        WorkoutTemplateExerciseEntity::class,
        ScheduleEntity::class,
        ScheduleItemEntity::class,
        WorkoutSessionEntity::class,
        SessionExerciseEntity::class,
        SetEntryEntity::class,
        BodyWeightEntryEntity::class,
        ProgressPhotoEntity::class,
        ActivityLogEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class HoofModeDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutTemplateDao(): WorkoutTemplateDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun sessionExerciseDao(): SessionExerciseDao
    abstract fun setEntryDao(): SetEntryDao
    abstract fun bodyWeightEntryDao(): BodyWeightEntryDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
    abstract fun activityLogDao(): ActivityLogDao
}
