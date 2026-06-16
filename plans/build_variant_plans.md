# 開発用 / 本番用アプリの分離計画（モジュール分割 + Koin override）

開発体験向上のため、**開発用アプリ（development）** と **本番用アプリ（production）** を同一端末に並存インストールできるようにする。
やりたいことは大きく2つ。

1. **DBの差し替え**: dev と prod で別々のデータベースを使う（dev 作業が本番データを壊さない）。
2. **Repository などの差し替え**: dev では本番用 Repository を別実装（フェイク／シード入りなど）に差し替えられるようにする。

実現方式は **Gradle モジュールの分割**（`:app:production` / `:app:development`）+ 共通 library `:core:application`。
Repository の差し替えは、参考リポジトリ（`maigo_compass_android`）と同じ **「BaseApplication が本番実装を Koin に登録し、開発版が override する」** パターンを採る。

## 確定した方針

| 項目 | 決定 |
|------|------|
| dev/prod の分離 | **別 application モジュール**（`:app:production` / `:app:development`）+ 共通 library `:core:application` |
| dev の applicationId | `net.yourein.rebro.dev`（`prod` は `net.yourein.rebro`）→ 並存インストール可 |
| Repository 差し替え | `BaseApplication` が本番を登録、`DevelopmentApplication` が `koinStarter()` を override し `loadKoinModules` で上書き |
| DB 名の与え方 | **`BuildConfig.DATABASE_NAME`（config 駆動）**。各アプリモジュールで `buildConfigField` 定義（要 `buildConfig = true`） |
| モックの置き場所 | **`:app:development` 内 `mock/`**（`net.yourein.rebro.mock`） |
| dev の初期状態 | **まず配線だけ。`loadKoinModules` は空で開始し実 Repository で動かす**。モックは画面ができ次第 後追い |
| ランチャー上の識別 | **アプリ名のみ変更**（`Rebro (Dev)`）。アイコンは prod と共通 |

---

## モジュール構成

```
:core:application   (com.android.library)
    ├─ BaseApplication.kt        … 抽象 Application。本番 Repository を含む全モジュールを Koin に登録
    ├─ MainActivity.kt           … 共通の起動 Activity（ランチャー）
    └─ di/
        ├─ RepositoryModule.kt   … repositoryKoinModule（★本番=実 Impl。共通側に置く）
        ├─ UseCaseModule.kt      … useCaseKoinModule（共通）
        └─ ViewModelModule.kt    … viewModelKoinModule（共通）

:app:production     (com.android.application)  applicationId = net.yourein.rebro
    └─ ProductionApplication.kt  … databaseName を BuildConfig.DATABASE_NAME で override するのみ

:app:development    (com.android.application)  applicationId = net.yourein.rebro.dev
    ├─ DevelopmentApplication.kt … databaseName override + koinStarter() override（当面 loadKoinModules は空）
    └─ mock/                      … フェイク Repository（後追いで追加）
        ├─ FakeBookRepository.kt
        ├─ FakeBookshelfRepository.kt
        └─ FakeAuthorRepository.kt
```

依存関係:

```
:core:application ──> :model / :interfaces / :core:resources / :repositories（実 Repository 実装）/ feature:*

:app:production  ──> :core:application
                     （実 Repository は :core:application の implementation 依存として実行時に同梱される）

:app:development ──> :core:application
                 ──> :interfaces / :model        （モックが参照する。モック追加時に使用）
                     （:repositories には依存しない。モックは :app:development 内に置く）
```

> **要点**: モックは `:app:development` にしか存在せず、`:app:production` はそれに依存しない。
> よって**本番 APK にモックが一切含まれない**。
> 開発 APK には「実 Impl（`:core:application` 経由）＋ モック」の両方が含まれるが、
> 起動時に Koin 上でモックが本番定義を override するため、実際に使われるのはモックになる（モック追加後）。

### namespace について

両アプリモジュールとも `namespace = "net.yourein.rebro"` とする。
各 `Application` クラスは **自モジュールの** `net.yourein.rebro.BuildConfig` を参照するため、これで `BuildConfig.DATABASE_NAME` がそのまま読める。
2つの application モジュールが同一 namespace を持つが、両者は同じ依存グラフに同居しない（`production` は `development` に依存しない）ため、R / BuildConfig の衝突は起きない。

---

## 1. 共通モジュール `:core:application`

### settings.gradle.kts

```kotlin
include(":core:application")
include(":app:production")
include(":app:development")
// 既存の include(":app") は廃止し、上記へ再構成する
```

### `:core:application` の build.gradle.kts（library）

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "net.yourein.rebro.core.application"
    compileSdk = 36
    defaultConfig { minSdk = 33 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core:resources"))
    implementation(project(":model"))
    implementation(project(":interfaces"))
    implementation(project(":repositories"))   // 本番 Repository の実 Impl を登録するため
    implementation(libs.androidx.room.runtime)  // AppDatabase を構築するため

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
}
```

### BaseApplication.kt（抽象）

`koinStarter()` を `protected open` にし、**本番用の全モジュール**を登録する。
DB 名は `protected open val databaseName` として持ち、各アプリの `Application` が `BuildConfig.DATABASE_NAME` で override する。

```kotlin
package net.yourein.rebro.core.application

import android.app.Application
import androidx.room.Room
import net.yourein.rebro.core.application.di.repositoryKoinModule
import net.yourein.rebro.core.application.di.useCaseKoinModule
import net.yourein.rebro.core.application.di.viewModelKoinModule
import net.yourein.rebro.repositories.AppDatabase
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

    /**
     * 本番構成の Koin を起動する。
     * 開発版は本メソッドを override し、super.koinStarter() の後に
     * loadKoinModules() でモックを上書き登録する。
     */
    protected open fun koinStarter() {
        startKoin {
            androidLogger()
            androidContext(this@BaseApplication)
            modules(
                listOf(
                    databaseKoinModule,
                    repositoryKoinModule,   // 本番=実 Impl
                    useCaseKoinModule,
                    viewModelKoinModule,
                )
            )
        }
    }

    /**
     * DB 名が override 可能なよう、databaseName を参照する形でここに定義する。
     * （top-level val だと databaseName を参照できないため、インスタンスメンバとして持つ）
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
    }
}
```

### 共通 Koin モジュール（`:core:application/di/`）

```kotlin
// RepositoryModule.kt … 本番=実 Impl
package net.yourein.rebro.core.application.di

import net.yourein.rebro.interfaces.AuthorRepository
import net.yourein.rebro.interfaces.BookRepository
import net.yourein.rebro.interfaces.BookshelfRepository
import net.yourein.rebro.repositories.AuthorRepositoryImpl
import net.yourein.rebro.repositories.BookRepositoryImpl
import net.yourein.rebro.repositories.BookshelfRepositoryImpl
import org.koin.dsl.module

val repositoryKoinModule = module {
    single<BookshelfRepository> { BookshelfRepositoryImpl(get()) }
    single<BookRepository> { BookRepositoryImpl(get()) }
    single<AuthorRepository> { AuthorRepositoryImpl(get()) }
}
```

```kotlin
// UseCaseModule.kt
package net.yourein.rebro.core.application.di
import org.koin.dsl.module
/** UseCase 層。今後増えたら追加する。例: factory { SearchBooksUseCase(get()) } */
val useCaseKoinModule = module { }
```

```kotlin
// ViewModelModule.kt
package net.yourein.rebro.core.application.di
import org.koin.dsl.module
/** ViewModel 層。画面が増えたら追加する。例: viewModel { SearchTopViewModel(get()) } */
val viewModelKoinModule = module { }
```

### MainActivity.kt（共通）

現在 `:app` にある `MainActivity` を `:core:application`（パッケージ `net.yourein.rebro.core.application`）へ移設。
ランチャー定義は `:core:application` の Manifest に置き、各アプリモジュールへマージさせる。

```xml
<!-- :core:application/src/main/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application>
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Rebro">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## 2. 本番アプリモジュール `:app:production`

### build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "net.yourein.rebro"
    compileSdk { version = release(36) { minorApiLevel = 1 } }

    defaultConfig {
        applicationId = "net.yourein.rebro"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "DATABASE_NAME", "\"rebro.db\"")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true   // buildConfigField を使うのに必須
    }
}

dependencies {
    implementation(project(":core:application"))
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
}
```

### ProductionApplication.kt

config 駆動の DB 名を読むため、`databaseName` の override のみ持つ。

```kotlin
package net.yourein.rebro

import net.yourein.rebro.core.application.BaseApplication

class ProductionApplication : BaseApplication() {
    override val databaseName: String = BuildConfig.DATABASE_NAME
}
```

### AndroidManifest.xml

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:name=".ProductionApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Rebro" />
    <!-- MainActivity は :core:application の Manifest からマージされる -->
</manifest>
```

---

## 3. 開発アプリモジュール `:app:development`

### build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "net.yourein.rebro"
    compileSdk { version = release(36) { minorApiLevel = 1 } }

    defaultConfig {
        applicationId = "net.yourein.rebro.dev"   // ← 並存インストールの肝
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        versionNameSuffix = "-dev"                  // 任意
        buildConfigField("String", "DATABASE_NAME", "\"rebro-dev.db\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:application"))
    implementation(project(":interfaces"))   // モックが実装するインターフェース（モック追加時に使用）
    implementation(project(":model"))        // モックが扱うエンティティ
    implementation(libs.kotlinx.coroutines.core)  // Flow を返すフェイク用

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
}
```

### DevelopmentApplication.kt（初期状態 = 配線のみ）

まずは override の配線だけ用意し、`loadKoinModules` は空にして本番と同じ実 Repository で起動する。
DB は dev 専用名で分離される。

```kotlin
package net.yourein.rebro

import net.yourein.rebro.core.application.BaseApplication
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class DevelopmentApplication : BaseApplication() {

    // 開発用 DB（本番データと分離）
    override val databaseName: String = BuildConfig.DATABASE_NAME

    override fun koinStarter() {
        super.koinStarter()   // 本番構成を登録
        // モックは画面ができ次第ここに追加して本番定義を上書きする（下記「後追いのモック化」参照）
        loadKoinModules(
            listOf(
                module { }
            )
        )
    }
}
```

### 後追いのモック化（必要になったら）

画面開発を進める中でモックが必要になったら、`:app:development` 内に `mock/` を作り、フェイクを実装して
`loadKoinModules` の `module { }` に登録する。Koin は後勝ちで本番定義を override する。

```kotlin
// DevelopmentApplication.koinStarter() 内
loadKoinModules(
    listOf(
        module {
            single<BookshelfRepository> { FakeBookshelfRepository() }
            single<BookRepository> { FakeBookRepository() }
            single<AuthorRepository> { FakeAuthorRepository() }
        }
    )
)
```

```kotlin
// :app:development/.../mock/FakeBookRepository.kt（例）
package net.yourein.rebro.mock

import kotlinx.coroutines.flow.MutableStateFlow
import net.yourein.rebro.interfaces.BookRepository
import net.yourein.rebro.model.entity.Book
// ... BookRepository のシグネチャに合わせ、インメモリ + シードで実装

class FakeBookRepository : BookRepository {
    private val books = MutableStateFlow<List<Book>>(emptyList())
    // 各メソッドを books ベースで実装
}
```

> **Koin の override について**: Koin 3.2 以降（本プロジェクトは 4.x）では、`loadKoinModules` で
> 既存と同じ型（＋qualifier）を再定義すると、明示フラグなしで **後勝ちで override** される。
> `super.koinStarter()` 直後にロードするため、いずれの定義もまだ解決（生成）されておらず安全に差し替わる。
> モックを `single` にすると、インメモリのシード状態が注入間で共有される。

### AndroidManifest.xml / アプリ名

```xml
<application
    android:name=".DevelopmentApplication"
    android:label="@string/app_name_dev"
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.Rebro" />
```

`:app:development/src/main/res/values/strings.xml`:

```xml
<resources>
    <string name="app_name_dev">Rebro (Dev)</string>
</resources>
```

---

## 4. 既存 `:app` からの移行手順

1. `:core:application`（library）を新規作成。
2. 既存 `:app` の `MainActivity` を `:core:application` へ移設。ランチャー定義も同モジュールの Manifest へ。
3. 既存 `RebroApplication` を分解:
   - 共通（database / repository(本番) / useCase / viewModel）→ `:core:application` の `BaseApplication` + `di/`。
   - `RebroApplication` 自体は破棄し、抽象 `BaseApplication` に置き換え。
4. `:app:production`（`ProductionApplication` + `buildConfigField`）を作成。
5. `:app:development`（`DevelopmentApplication` + 空の `loadKoinModules` + `buildConfigField`）を作成。
6. `:app` を削除し、`settings.gradle.kts` の `include(":app")` を 3 モジュールへ差し替え。
7. `:app:production` / `:app:development` をそれぞれ実行構成として登録し、並存インストールを確認。

---

## 実装チェックリスト

- [x] `:core:application`（library）を作成し、`BaseApplication`・`MainActivity`・共通 Koin モジュールを集約
- [x] `BaseApplication.koinStarter()` を `protected open` で実装し、本番 Repository を含む全モジュールを登録
- [x] `BaseApplication.databaseName`（`protected open`、既定 `"rebro.db"`）で DB 名を override 可能にする
- [x] `:core:application` の Manifest にランチャー `MainActivity` を定義
- [x] `:app:production` を作成（`applicationId = net.yourein.rebro`、`buildConfigField DATABASE_NAME = rebro.db`、`buildConfig = true`、`ProductionApplication` で databaseName override）
- [x] `:app:development` を作成（`applicationId = net.yourein.rebro.dev`、`buildConfigField DATABASE_NAME = rebro-dev.db`、`buildConfig = true`、`DevelopmentApplication` で databaseName override + 空 `loadKoinModules`）
- [x] dev のアプリ名 `@string/app_name_dev`（`Rebro (Dev)`）を上書き
- [x] 既存 `:app` を削除し `settings.gradle.kts` を 3 モジュールへ再構成
- [x] 両アプリの debug APK ビルドを確認（`assembleDebug` 成功・マージ後 Manifest で applicationId / Application / ランチャーを確認済み）。実機での並存インストール確認は未実施
- [ ]（後追い）必要な画面からモックを `:app:development/mock/` に実装し `loadKoinModules` に登録
