package net.yourein.rebro.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.model.entity.Author

@Dao
interface AuthorDao {
    @Query("SELECT * FROM authors ORDER BY name ASC")
    fun getAuthors(): Flow<List<Author>>

    @Query("SELECT * FROM authors WHERE id = :authorId")
    suspend fun getAuthor(authorId: Long): Author?

    @Query("SELECT * FROM authors WHERE name = :name LIMIT 1")
    suspend fun findAuthorByName(name: String): Author?

    @Insert
    suspend fun insertAuthor(author: Author): Long

    @Update
    suspend fun updateAuthor(author: Author)

    @Delete
    suspend fun deleteAuthor(author: Author)
}
