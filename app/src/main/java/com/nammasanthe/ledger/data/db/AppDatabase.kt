package com.nammasanthe.ledger.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nammasanthe.ledger.data.db.dao.CustomerDao
import com.nammasanthe.ledger.data.db.dao.ReminderLogDao
import com.nammasanthe.ledger.data.db.dao.TransactionDao
import com.nammasanthe.ledger.data.db.entity.CustomerEntity
import com.nammasanthe.ledger.data.db.entity.ReminderLogEntity
import com.nammasanthe.ledger.data.db.entity.TransactionEntity

class Converters {
    @TypeConverter
    fun fromType(value: TransactionType): String = value.name

    @TypeConverter
    fun toType(value: String): TransactionType = TransactionType.valueOf(value)
}

@Database(
    entities = [CustomerEntity::class, TransactionEntity::class, ReminderLogEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun transactionDao(): TransactionDao
    abstract fun reminderLogDao(): ReminderLogDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE customers ADD COLUMN phoneNumber TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN dueDays INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN dueDate INTEGER")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS reminder_logs (
                        customerId INTEGER NOT NULL PRIMARY KEY,
                        lastSmsSentAt INTEGER NOT NULL,
                        FOREIGN KEY(customerId) REFERENCES customers(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "namma_santhe_ledger.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
