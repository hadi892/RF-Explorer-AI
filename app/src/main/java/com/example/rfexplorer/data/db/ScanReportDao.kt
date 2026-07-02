package com.example.rfexplorer.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rfexplorer.data.model.ScanReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanReportDao {
    @Query("SELECT * FROM scan_reports ORDER BY timestampMs DESC")
    fun getAllReports(): Flow<List<ScanReportEntity>>

    @Query("SELECT * FROM scan_reports WHERE id = :reportId LIMIT 1")
    suspend fun getReportById(reportId: Long): ScanReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ScanReportEntity): Long

    @Query("DELETE FROM scan_reports WHERE id = :reportId")
    suspend fun deleteReportById(reportId: Long)

    @Query("DELETE FROM scan_reports")
    suspend fun deleteAllReports()
}
