package net.yourein.rebro

import net.yourein.rebro.core.application.BaseApplication
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class DevelopmentApplication : BaseApplication() {

    // 開発用 DB（本番データと分離）
    override val databaseName: String = BuildConfig.DATABASE_NAME

    override fun koinStarter() {
        super.koinStarter()   // 本番構成を登録
        // モックは画面ができ次第ここに追加して本番定義を上書きする。
        loadKoinModules(
            listOf(
                module { }
            )
        )
    }
}
