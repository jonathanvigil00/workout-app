package com.jvigil.hoofmode.data.repository

import com.jvigil.hoofmode.data.local.dao.BodyWeightEntryDao
import com.jvigil.hoofmode.data.local.entity.BodyWeightEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyWeightRepository @Inject constructor(private val dao: BodyWeightEntryDao) {

    fun observeAll(): Flow<List<BodyWeightEntryEntity>> = dao.observeAll()

    fun observeSince(from: LocalDate): Flow<List<BodyWeightEntryEntity>> = dao.observeSince(from)

    suspend fun getByDate(date: LocalDate): BodyWeightEntryEntity? = dao.getByDate(date)

    /** One entry per day: logging again today edits that day's row in place instead of adding a new one. */
    suspend fun logEntry(date: LocalDate, weight: Double, note: String?) {
        val existing = dao.getByDate(date)
        if (existing != null) {
            dao.update(existing.copy(weight = weight, note = note))
        } else {
            dao.insert(BodyWeightEntryEntity(date = date, weight = weight, note = note))
        }
    }

    suspend fun updateEntry(entry: BodyWeightEntryEntity) = dao.update(entry)

    suspend fun deleteEntry(entry: BodyWeightEntryEntity) = dao.delete(entry)
}
