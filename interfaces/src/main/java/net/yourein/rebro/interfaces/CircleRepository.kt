package net.yourein.rebro.interfaces

import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.model.entity.Circle

interface CircleRepository {
    fun getCircles(): Flow<List<Circle>>

    suspend fun getCircle(circleId: Long): Circle?

    suspend fun findCircleByName(name: String): Circle?

    suspend fun addCircle(circle: Circle): Long

    suspend fun updateCircle(circle: Circle)

    suspend fun deleteCircle(circle: Circle)
}
