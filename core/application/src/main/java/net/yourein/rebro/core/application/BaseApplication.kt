package net.yourein.rebro.core.application

import android.app.Application
import androidx.room.Room
import net.yourein.rebro.feature.bookdetail.BookDetailViewModel
import net.yourein.rebro.feature.circles.CirclesViewModel
import net.yourein.rebro.feature.registertop.RegisterTopViewModel
import net.yourein.rebro.feature.search.SearchViewModel
import net.yourein.rebro.feature.searchtop.SearchTopViewModel
import net.yourein.rebro.interfaces.AuthorRepository
import net.yourein.rebro.interfaces.BookRepository
import net.yourein.rebro.interfaces.BookshelfRepository
import net.yourein.rebro.interfaces.CircleRepository
import net.yourein.rebro.repositories.AppDatabase
import net.yourein.rebro.repositories.AuthorRepositoryImpl
import net.yourein.rebro.repositories.BookRepositoryImpl
import net.yourein.rebro.repositories.BookshelfRepositoryImpl
import net.yourein.rebro.repositories.CircleRepositoryImpl
import net.yourein.rebro.usecase.BooksUseCase
import net.yourein.rebro.usecase.BookshelfUseCase
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

abstract class BaseApplication : Application() {

    /**
     * 使用する DB ファイル名。各アプリの Application が BuildConfig.DATABASE_NAME で override する。
     * （未 override 時のフォールバックとして本番名を既定値に持つ）
     */
    protected open val databaseName: String = "rebro.db"

    override fun onCreate() {
        super.onCreate()
        koinStarter()
    }

    protected open fun koinStarter() {
        startKoin {
            androidLogger()
            androidContext(this@BaseApplication)
            modules(
                listOf(
                    databaseKoinModule,
                    repositoryKoinModule,
                    useCaseKoinModule,
                    viewModelKoinModule,
                )
            )
        }
    }

    /**
     * DB・DAO などインフラ層の依存。
     * AppDatabase はアプリ全体で1インスタンスだけ存在すればよいため single で保持し、各 DAO をそこから取り出す。
     * DB 名を override 可能なよう databaseName を参照する形でインスタンスメンバとして持つ。
     */
    private val databaseKoinModule = module {
        single {
            Room.databaseBuilder(
                androidContext(),
                AppDatabase::class.java,
                databaseName,
            ).build()
        }
        single { get<AppDatabase>().bookshelfDao() }
        single { get<AppDatabase>().bookDao() }
        single { get<AppDatabase>().authorDao() }
        single { get<AppDatabase>().circleDao() }
    }

    private val repositoryKoinModule = module {
        factory<BookshelfRepository> { BookshelfRepositoryImpl(get()) }
        factory<BookRepository> { BookRepositoryImpl(get()) }
        factory<AuthorRepository> { AuthorRepositoryImpl(get()) }
        factory<CircleRepository> { CircleRepositoryImpl(get()) }
    }

    private val useCaseKoinModule = module {
        factory<BooksUseCase> { BooksUseCase(get()) }
        factory<BookshelfUseCase> { BookshelfUseCase(get()) }
    }

    private val viewModelKoinModule = module {
        factory<SearchTopViewModel> { SearchTopViewModel(get()) }
        factory<SearchViewModel> { SearchViewModel(get()) }
        factory<BookDetailViewModel> { (bookId: Long) -> BookDetailViewModel(bookId, get(), get()) }
        factory<CirclesViewModel> { CirclesViewModel(get()) }
        factory<RegisterTopViewModel> { RegisterTopViewModel(androidApplication(), get(), get(), get(), get()) }
    }
}
