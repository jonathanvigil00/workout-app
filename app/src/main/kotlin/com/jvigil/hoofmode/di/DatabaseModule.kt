package com.jvigil.hoofmode.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jvigil.hoofmode.data.local.HoofModeDatabase
import com.jvigil.hoofmode.data.local.MIGRATION_1_2
import com.jvigil.hoofmode.data.local.SeedExercises
import com.jvigil.hoofmode.data.local.dao.ActivityLogDao
import com.jvigil.hoofmode.data.local.dao.BodyWeightEntryDao
import com.jvigil.hoofmode.data.local.dao.ExerciseDao
import com.jvigil.hoofmode.data.local.dao.ProgressPhotoDao
import com.jvigil.hoofmode.data.local.dao.ScheduleDao
import com.jvigil.hoofmode.data.local.dao.SessionExerciseDao
import com.jvigil.hoofmode.data.local.dao.SetEntryDao
import com.jvigil.hoofmode.data.local.dao.WorkoutSessionDao
import com.jvigil.hoofmode.data.local.dao.WorkoutTemplateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        exerciseDaoProvider: Provider<ExerciseDao>,
        @ApplicationScope applicationScope: CoroutineScope,
    ): HoofModeDatabase =
        Room.databaseBuilder(context, HoofModeDatabase::class.java, "hoofmode.db")
            .addMigrations(MIGRATION_1_2)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    applicationScope.launch {
                        exerciseDaoProvider.get().let { dao ->
                            SeedExercises.all().forEach { dao.insert(it) }
                        }
                    }
                }
            })
            .build()

    @Provides
    fun provideExerciseDao(db: HoofModeDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    fun provideWorkoutTemplateDao(db: HoofModeDatabase): WorkoutTemplateDao = db.workoutTemplateDao()

    @Provides
    fun provideScheduleDao(db: HoofModeDatabase): ScheduleDao = db.scheduleDao()

    @Provides
    fun provideWorkoutSessionDao(db: HoofModeDatabase): WorkoutSessionDao = db.workoutSessionDao()

    @Provides
    fun provideSessionExerciseDao(db: HoofModeDatabase): SessionExerciseDao = db.sessionExerciseDao()

    @Provides
    fun provideSetEntryDao(db: HoofModeDatabase): SetEntryDao = db.setEntryDao()

    @Provides
    fun provideBodyWeightEntryDao(db: HoofModeDatabase): BodyWeightEntryDao = db.bodyWeightEntryDao()

    @Provides
    fun provideProgressPhotoDao(db: HoofModeDatabase): ProgressPhotoDao = db.progressPhotoDao()

    @Provides
    fun provideActivityLogDao(db: HoofModeDatabase): ActivityLogDao = db.activityLogDao()
}
