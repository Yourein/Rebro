package net.yourein.rebro.repositories

import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.database.AuthorDao
import net.yourein.rebro.interfaces.AuthorRepository
import net.yourein.rebro.model.entity.Author

class AuthorRepositoryImpl(
    private val authorDao: AuthorDao,
) : AuthorRepository {

    override fun getAuthors(): Flow<List<Author>> =
        authorDao.getAuthors()

    override suspend fun getAuthor(authorId: Long): Author? =
        authorDao.getAuthor(authorId)

    override suspend fun findAuthorByName(name: String): Author? =
        authorDao.findAuthorByName(name)

    override suspend fun addAuthor(author: Author): Long =
        authorDao.insertAuthor(author)

    override suspend fun updateAuthor(author: Author) =
        authorDao.updateAuthor(author)

    override suspend fun deleteAuthor(author: Author) =
        authorDao.deleteAuthor(author)
}
