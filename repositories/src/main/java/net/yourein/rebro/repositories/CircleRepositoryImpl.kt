package net.yourein.rebro.repositories

import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.interfaces.CircleRepository
import net.yourein.rebro.model.entity.Circle

class CircleRepositoryImpl(
    private val circleDao: CircleDao,
) : CircleRepository {

    override fun getCircles(): Flow<List<Circle>> =
        circleDao.getCircles()

    override suspend fun getCircle(circleId: Long): Circle? =
        circleDao.getCircle(circleId)

    override suspend fun findCircleByName(name: String): Circle? =
        circleDao.findCircleByName(name)

    override suspend fun addCircle(circle: Circle): Long =
        circleDao.insertCircle(circle)

    override suspend fun updateCircle(circle: Circle) =
        circleDao.updateCircle(circle)

    override suspend fun deleteCircle(circle: Circle) =
        circleDao.deleteCircle(circle)
}
