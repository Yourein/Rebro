package net.yourein.rebro.interfaces

import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.model.entity.Author

interface AuthorRepository {
    fun getAuthors(): Flow<List<Author>>

    suspend fun getAuthor(authorId: Long): Author?

    /** 著者名（UNIQUE）で1件検索する。存在しなければ null。 */
    suspend fun findAuthorByName(name: String): Author?

    suspend fun addAuthor(author: Author): Long

    suspend fun updateAuthor(author: Author)

    suspend fun deleteAuthor(author: Author)
}
