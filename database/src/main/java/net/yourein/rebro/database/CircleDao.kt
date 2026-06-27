package net.yourein.rebro.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.model.entity.Circle

@Dao
interface CircleDao {
    @Query("SELECT * FROM circles ORDER BY name ASC")
    fun getCircles(): Flow<List<Circle>>

    @Query("SELECT * FROM circles WHERE id = :circleId")
    suspend fun getCircle(circleId: Long): Circle?

    @Query("SELECT * FROM circles WHERE name = :name LIMIT 1")
    suspend fun findCircleByName(name: String): Circle?

    @Insert
    suspend fun insertCircle(circle: Circle): Long

    @Update
    suspend fun updateCircle(circle: Circle)

    @Delete
    suspend fun deleteCircle(circle: Circle)
}
