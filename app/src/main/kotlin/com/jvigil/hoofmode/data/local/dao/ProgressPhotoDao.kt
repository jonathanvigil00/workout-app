package com.jvigil.hoofmode.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jvigil.hoofmode.data.local.entity.ProgressPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressPhotoDao {

    @Query("SELECT * FROM progress_photos ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<ProgressPhotoEntity>>

    @Query("SELECT * FROM progress_photos WHERE id = :id")
    suspend fun getById(id: Long): ProgressPhotoEntity?

    @Insert
    suspend fun insert(photo: ProgressPhotoEntity): Long

    @Update
    suspend fun update(photo: ProgressPhotoEntity)

    @Delete
    suspend fun delete(photo: ProgressPhotoEntity)
}
