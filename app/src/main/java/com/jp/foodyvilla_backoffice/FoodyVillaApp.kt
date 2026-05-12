package com.jp.foodyvilla_backoffice

import android.app.Application
import com.jp.foodyvilla_backoffice.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FoodyVillaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@FoodyVillaApp)
            modules(appModule)
        }

    }
}