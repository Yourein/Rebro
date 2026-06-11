package net.yourein.rebro.model.converter

import androidx.room.TypeConverter
import net.yourein.rebro.model.BookType
import net.yourein.rebro.model.ReadingStatus

class Converters {
    @TypeConverter
    fun fromBookType(value: BookType): String = value.name

    @TypeConverter
    fun toBookType(value: String): BookType = BookType.valueOf(value)

    @TypeConverter
    fun fromReadingStatus(value: ReadingStatus): String = value.name

    @TypeConverter
    fun toReadingStatus(value: String): ReadingStatus = ReadingStatus.valueOf(value)
}
