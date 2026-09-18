package com.jvigil.hoofmode.data.local

import androidx.room.TypeConverter
import com.jvigil.hoofmode.data.local.entity.ActivityType
import com.jvigil.hoofmode.data.local.entity.OrderMode
import com.jvigil.hoofmode.data.local.entity.PhotoTag
import com.jvigil.hoofmode.data.local.entity.ScheduleItemType
import java.time.Instant
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun instantToEpochMillis(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun epochMillisToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun localDateToIso(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun isoToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun orderModeToString(value: OrderMode): String = value.name

    @TypeConverter
    fun stringToOrderMode(value: String): OrderMode = OrderMode.valueOf(value)

    @TypeConverter
    fun scheduleItemTypeToString(value: ScheduleItemType): String = value.name

    @TypeConverter
    fun stringToScheduleItemType(value: String): ScheduleItemType = ScheduleItemType.valueOf(value)

    @TypeConverter
    fun activityTypeToString(value: ActivityType): String = value.name

    @TypeConverter
    fun stringToActivityType(value: String): ActivityType = ActivityType.valueOf(value)

    @TypeConverter
    fun photoTagsToString(value: Set<PhotoTag>): String = value.joinToString(",") { it.name }

    @TypeConverter
    fun stringToPhotoTags(value: String): Set<PhotoTag> =
        if (value.isBlank()) emptySet() else value.split(",").map(PhotoTag::valueOf).toSet()
}
