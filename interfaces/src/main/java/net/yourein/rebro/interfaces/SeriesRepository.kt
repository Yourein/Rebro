package net.yourein.rebro.interfaces

import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.model.entity.Series

interface SeriesRepository {
    fun getSeries(): Flow<List<Series>>

    suspend fun getSeriesById(seriesId: Long): Series?

    suspend fun findSeriesByName(name: String): Series?

    suspend fun addSeries(series: Series): Long

    suspend fun updateSeries(series: Series)

    suspend fun deleteSeries(series: Series)
}
