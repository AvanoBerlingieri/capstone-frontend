package capstone.safeline.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import capstone.safeline.data.local.dao.MessageDao
import capstone.safeline.data.local.entity.FriendEntity
import capstone.safeline.data.local.entity.GroupChatEntity
import capstone.safeline.data.local.entity.GroupChatMemberEntity
import capstone.safeline.data.local.entity.GroupMessageEntity
import capstone.safeline.data.local.entity.MessageEntity

@Database(
    entities = [
        MessageEntity::class,
        GroupMessageEntity::class,
        GroupChatMemberEntity::class,
        GroupChatEntity::class,
        FriendEntity::class
    ],
    version = 3
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

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
                    .fallbackToDestructiveMigration() // to update tables
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}