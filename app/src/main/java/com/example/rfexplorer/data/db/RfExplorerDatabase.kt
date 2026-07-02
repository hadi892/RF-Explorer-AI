package com.example.rfexplorer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.rfexplorer.data.model.ScanReportEntity

@Database(entities = [ScanReportEntity::class], version = 1, exportSchema = false)
abstract class RfExplorerDatabase : RoomDatabase() {
    abstract fun scanReportDao(): ScanReportDao

    companion object {
        @Volatile
        private var INSTANCE: RfExplorerDatabase? = null

        fun getDatabase(context: Context): RfExplorerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RfExplorerDatabase::class.java,
                    "rf_explorer_ai.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
