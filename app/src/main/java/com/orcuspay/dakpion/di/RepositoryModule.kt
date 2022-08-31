package com.orcuspay.dakpion.di

import com.orcuspay.dakpion.data.repository.DakpionRepositoryImp
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDakpionRepository(
        dakpionRepository: DakpionRepositoryImp
    ): DakpionRepository
}