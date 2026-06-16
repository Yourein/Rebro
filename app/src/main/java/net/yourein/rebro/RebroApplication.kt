package net.yourein.rebro

import android.app.Application
import androidx.room.Room
import net.yourein.rebro.interfaces.AuthorRepository
import net.yourein.rebro.interfaces.BookRepository
import net.yourein.rebro.interfaces.BookshelfRepository
import net.yourein.rebro.repositories.AppDatabase
import net.yourein.rebro.repositories.AuthorRepositoryImpl
import net.yourein.rebro.repositories.BookRepositoryImpl
import net.yourein.rebro.repositories.BookshelfRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class RebroApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        koinStarter()
    }

    private fun koinStarter() {
        startKoin {
            androidLogger()
            androidContext(this@RebroApplication)
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
     * AppDatabase はアプリ全体で1インスタンスだけ存在すればよいため single で保持し、
     * 各 DAO はそこから取り出して提供する。
     */
    private val databaseKoinModule = module {
        single {
            Room.databaseBuilder(
                androidContext(),
                AppDatabase::class.java,
                "rebro.db",
            ).build()
        }
        single { get<AppDatabase>().bookshelfDao() }
        single { get<AppDatabase>().bookDao() }
        single { get<AppDatabase>().authorDao() }
    }

    /**
     * Repository 層。DAO を受け取り、interface 型として公開する。
     */
    private val repositoryKoinModule = module {
        single<BookshelfRepository> { BookshelfRepositoryImpl(get()) }
        single<BookRepository> { BookRepositoryImpl(get()) }
        single<AuthorRepository> { AuthorRepositoryImpl(get()) }
    }

    /**
     * UseCase 層。今後 UseCase が増えたらここに追加する。
     * 例: factory { SearchBooksUseCase(get()) }
     */
    private val useCaseKoinModule = module {
    }

    /**
     * ViewModel 層。画面ごとの ViewModel が増えたらここに追加する。
     * `viewModel { ... }` DSL は koin-androidx-compose / koin-android が提供する。
     * 例: viewModel { SearchTopViewModel(get()) }
     */
    private val viewModelKoinModule = module {
    }
}
