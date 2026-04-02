package capstone.safeline.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import capstone.safeline.data.local.dao.GroupMessageDao
import capstone.safeline.data.local.dao.MessageDao
import capstone.safeline.data.local.entity.GroupMessageEntity
import capstone.safeline.data.local.entity.MessageEntity

@Database(entities = [MessageEntity::class, GroupMessageEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun groupMessageDao(): GroupMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "safeline_database"
                )
                    .fallbackToDestructiveMigration() // Important for your time crunch!
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}