package net.yourein.rebro.repositories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.model.entity.Bookshelf

@Dao
interface BookshelfDao {
    @Query("SELECT * FROM bookshelves ORDER BY id ASC")
    fun getBookshelves(): Flow<List<Bookshelf>>

    @Query("SELECT * FROM bookshelves WHERE id = :bookshelfId")
    suspend fun getBookshelf(bookshelfId: Long): Bookshelf?

    @Insert
    suspend fun insertBookshelf(bookshelf: Bookshelf): Long

    @Update
    suspend fun updateBookshelf(bookshelf: Bookshelf)

    @Delete
    suspend fun deleteBookshelf(bookshelf: Bookshelf)
}
