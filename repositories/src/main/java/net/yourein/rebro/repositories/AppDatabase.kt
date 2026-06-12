package net.yourein.rebro.repositories

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.yourein.rebro.model.converter.Converters
import net.yourein.rebro.model.entity.Author
import net.yourein.rebro.model.entity.Book
import net.yourein.rebro.model.entity.BookAuthor
import net.yourein.rebro.model.entity.Bookshelf
import net.yourein.rebro.model.entity.CommercialBookDetail
import net.yourein.rebro.model.entity.DoujinBookDetail

@Database(
    entities = [
        Bookshelf::class,
        Book::class,
        CommercialBookDetail::class,
        DoujinBookDetail::class,
        Author::class,
        BookAuthor::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookshelfDao(): BookshelfDao
    abstract fun bookDao(): BookDao
    abstract fun authorDao(): AuthorDao
}
