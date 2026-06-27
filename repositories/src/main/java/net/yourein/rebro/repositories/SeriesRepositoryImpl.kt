package net.yourein.rebro.repositories

import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.database.SeriesDao
import net.yourein.rebro.interfaces.SeriesRepository
import net.yourein.rebro.model.entity.Series

class SeriesRepositoryImpl(
    private val seriesDao: SeriesDao,
) : SeriesRepository {

    override fun getSeries(): Flow<List<Series>> =
        seriesDao.getSeries()

    override suspend fun getSeriesById(seriesId: Long): Series? =
        seriesDao.getSeriesById(seriesId)

    override suspend fun findSeriesByName(name: String): Series? =
        seriesDao.findSeriesByName(name)

    override suspend fun addSeries(series: Series): Long =
        seriesDao.insertSeries(series)

    override suspend fun updateSeries(series: Series) =
        seriesDao.updateSeries(series)

    override suspend fun deleteSeries(series: Series) =
        seriesDao.deleteSeries(series)
}
