package com.jvigil.hoofmode.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jvigil.hoofmode.data.local.entity.BodyWeightEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface BodyWeightEntryDao {

    @Query("SELECT * FROM body_weight_entries ORDER BY date DESC")
    fun observeAll(): Flow<List<BodyWeightEntryEntity>>

    @Query("SELECT * FROM body_weight_entries WHERE date >= :from ORDER BY date ASC")
    fun observeSince(from: LocalDate): Flow<List<BodyWeightEntryEntity>>

    @Query("SELECT * FROM body_weight_entries WHERE date = :date")
    suspend fun getByDate(date: LocalDate): BodyWeightEntryEntity?

    @Query("SELECT * FROM body_weight_entries WHERE id = :id")
    suspend fun getById(id: Long): BodyWeightEntryEntity?

    @Insert
    suspend fun insert(entry: BodyWeightEntryEntity): Long

    @Update
    suspend fun update(entry: BodyWeightEntryEntity)

    @Delete
    suspend fun delete(entry: BodyWeightEntryEntity)
}
