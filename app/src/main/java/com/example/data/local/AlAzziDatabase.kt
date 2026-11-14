package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class PosTypeConverters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        runCatching { TransactionType.valueOf(value) }.getOrDefault(TransactionType.DEBT)
}

@Database(
    entities = [CustomerEntity::class, TransactionEntity::class, LessonProgressEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(PosTypeConverters::class)
abstract class AlAzziDatabase : RoomDatabase() {
    abstract fun posDao(): PosDao

    companion object {
        @Volatile
        private var INSTANCE: AlAzziDatabase? = null

        fun getInstance(context: Context): AlAzziDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AlAzziDatabase::class.java,
                    "al_azzi_pos_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
