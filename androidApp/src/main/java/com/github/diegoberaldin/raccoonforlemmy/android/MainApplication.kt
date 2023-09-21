package com.github.diegoberaldin.raccoonforlemmy.android

import android.app.Application
import com.github.diegoberaldin.racconforlemmy.core.utils.AppInfo
import com.github.diegoberaldin.raccoonforlemmy.di.sharedHelperModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            androidLogger()
            modules(
                sharedHelperModule,
            )

            AppInfo.versionCode = buildString {
                append(BuildConfig.VERSION_NAME)
                append(" (")
                append(BuildConfig.VERSION_CODE)
                append(")")
            }
        }
    }
}
