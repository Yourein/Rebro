package net.yourein.rebro.core.application

import android.app.Application
import androidx.room.Room
import net.yourein.rebro.database.AppDatabase
import net.yourein.rebro.feature.authors.AuthorsViewModel
import net.yourein.rebro.feature.bookdetail.BookDetailViewModel
import net.yourein.rebro.feature.books.BooksViewModel
import net.yourein.rebro.feature.bookshelfs.BookshelvesViewModel
import net.yourein.rebro.feature.circles.CirclesViewModel
import net.yourein.rebro.feature.registertop.IsdnDebugViewModel
import net.yourein.rebro.feature.registertop.RegisterTopViewModel
import net.yourein.rebro.feature.search.SearchViewModel
import net.yourein.rebro.feature.searchtop.SearchTopViewModel
import net.yourein.rebro.feature.series.SeriesViewModel
import net.yourein.rebro.interfaces.AuthorRepository
import net.yourein.rebro.interfaces.BookRepository
import net.yourein.rebro.interfaces.BookshelfRepository
import net.yourein.rebro.interfaces.CircleRepository
import net.yourein.rebro.interfaces.IsdnRepository
import net.yourein.rebro.interfaces.NdlRepository
import net.yourein.rebro.interfaces.SeriesRepository
import net.yourein.rebro.repositories.AuthorRepositoryImpl
import net.yourein.rebro.repositories.BookRepositoryImpl
import net.yourein.rebro.repositories.BookshelfRepositoryImpl
import net.yourein.rebro.repositories.CircleRepositoryImpl
import net.yourein.rebro.repositories.IsdnApiService
import net.yourein.rebro.repositories.IsdnRepositoryImpl
import net.yourein.rebro.repositories.NdlApiService
import net.yourein.rebro.repositories.NdlRepositoryImpl
import net.yourein.rebro.repositories.SeriesRepositoryImpl
import net.yourein.rebro.usecase.BooksUseCase
import net.yourein.rebro.usecase.BookshelfUseCase
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

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
                    networkKoinModule,
                    databaseKoinModule,
                    repositoryKoinModule,
                    useCaseKoinModule,
                    viewModelKoinModule,
                )
            )
        }
    }

    private val networkKoinModule = module {
        single {
            OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                )
                .build()
        }
        single {
            Retrofit.Builder()
                .baseUrl("https://isdn.jp/")
                .client(get<OkHttpClient>())
                .build()
                .create(IsdnApiService::class.java)
        }
        single {
            val ndlClient = get<OkHttpClient>().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
            Retrofit.Builder()
                .baseUrl("https://ndlsearch.ndl.go.jp/")
                .client(ndlClient)
                .build()
                .create(NdlApiService::class.java)
        }
        single {
            XML {
                recommended()
                policy = DefaultXmlSerializationPolicy.Builder().apply {
                    pedantic = false
                    unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
                }.build()
            }
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
        single { get<AppDatabase>().seriesDao() }
    }

    private val repositoryKoinModule = module {
        factory<BookshelfRepository> { BookshelfRepositoryImpl(get()) }
        factory<BookRepository> { BookRepositoryImpl(get()) }
        factory<AuthorRepository> { AuthorRepositoryImpl(get()) }
        factory<CircleRepository> { CircleRepositoryImpl(get()) }
        factory<SeriesRepository> { SeriesRepositoryImpl(get()) }
        factory<IsdnRepository> { IsdnRepositoryImpl(get(), get()) }
        factory<NdlRepository> { NdlRepositoryImpl(get(), get()) }
    }

    private val useCaseKoinModule = module {
        factory<BooksUseCase> { BooksUseCase(get()) }
        factory<BookshelfUseCase> { BookshelfUseCase(get()) }
    }

    private val viewModelKoinModule = module {
        factory<SearchTopViewModel> { SearchTopViewModel(get()) }
        factory<SearchViewModel> { SearchViewModel(get()) }
        factory<BookDetailViewModel> { (bookId: Long) -> BookDetailViewModel(bookId, get(), get(), get(), get(), get(), get(), get()) }
        factory<BooksViewModel> { BooksViewModel(get()) }
        factory<BookshelvesViewModel> { BookshelvesViewModel(get()) }
        factory<AuthorsViewModel> { AuthorsViewModel(get()) }
        factory<CirclesViewModel> { CirclesViewModel(get()) }
        factory<SeriesViewModel> { SeriesViewModel(get()) }
        factory<RegisterTopViewModel> { RegisterTopViewModel(androidApplication(), get(), get(), get(), get(), get()) }
        factory<IsdnDebugViewModel> { IsdnDebugViewModel(get(), get()) }
    }
}
