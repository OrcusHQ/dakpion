package com.orcuspay.dakpion.di

import android.app.Application
import androidx.room.Room
import com.orcuspay.dakpion.data.local.DakpionDatabase
import com.orcuspay.dakpion.data.remote.DakpionApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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
        val okHttpClient = OkHttpClient.Builder().build()
        return Retrofit.Builder()
            .baseUrl(DakpionApi.BASE_URL)
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
            .addMigrations()
            .build()
    }
}