package net.yourein.rebro.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.model.entity.Series

@Dao
interface SeriesDao {
    @Query("SELECT * FROM series ORDER BY name ASC")
    fun getSeries(): Flow<List<Series>>

    @Query("SELECT * FROM series WHERE id = :seriesId")
    suspend fun getSeriesById(seriesId: Long): Series?

    @Query("SELECT * FROM series WHERE name = :name LIMIT 1")
    suspend fun findSeriesByName(name: String): Series?

    @Insert
    suspend fun insertSeries(series: Series): Long

    @Update
    suspend fun updateSeries(series: Series)

    @Delete
    suspend fun deleteSeries(series: Series)
}
