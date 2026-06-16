package net.yourein.rebro

import net.yourein.rebro.core.application.BaseApplication

class ProductionApplication : BaseApplication() {
    override val databaseName: String = BuildConfig.DATABASE_NAME
}
