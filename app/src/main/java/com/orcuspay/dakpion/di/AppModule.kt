package com.orcuspay.dakpion.di

import android.app.Application
import androidx.room.Room
import com.orcuspay.dakpion.data.local.Converters
import com.orcuspay.dakpion.data.local.DakpionDatabase
import com.orcuspay.dakpion.data.remote.DakpionApi
import com.orcuspay.dakpion.data.remote.retrofit.NetworkResponseAdapterFactory
import com.orcuspay.dakpion.util.DakpionPreference
import com.orcuspay.dakpion.util.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideString(): String {
        return "Dakpion"
    }

    @Provides
    @Singleton
    fun provideStockApi(): DakpionApi {
        val okHttpClient = OkHttpClient.Builder().apply {
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
            addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(request)
            })
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
            .build()
        return Retrofit.Builder()
            .baseUrl(DakpionApi.BASE_URL)
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(DakpionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDakpionDatabase(app: Application): DakpionDatabase {
        return Room.databaseBuilder(
            app,
            DakpionDatabase::class.java,
            "dakpion.db"
        )
            .addTypeConverter(Converters())
            .addMigrations()
            .build()
    }

    @Provides
    @Singleton
    fun provideDakpionPreference(
        app: Application
    ): DakpionPreference {
        return DakpionPreference(app.applicationContext)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(
        app: Application
    ): NotificationHelper {
        return NotificationHelper(app.applicationContext)
    }
}