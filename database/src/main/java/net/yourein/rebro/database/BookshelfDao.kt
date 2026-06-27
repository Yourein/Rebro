package net.yourein.rebro.database

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

    /** 指定 ID の書籍が属する本棚を取得する（書籍 → 本棚の逆引き）。 */
    @Query(
        """
        SELECT bookshelves.* FROM bookshelves
        INNER JOIN books ON books.bookshelf_id = bookshelves.id
        WHERE books.id = :bookId
        """
    )
    suspend fun getBookshelfByBook(bookId: Long): Bookshelf?

    @Query("SELECT * FROM bookshelves WHERE name = :name LIMIT 1")
    suspend fun findBookshelfByName(name: String): Bookshelf?

    @Insert
    suspend fun insertBookshelf(bookshelf: Bookshelf): Long

    @Update
    suspend fun updateBookshelf(bookshelf: Bookshelf)

    @Delete
    suspend fun deleteBookshelf(bookshelf: Bookshelf)
}
