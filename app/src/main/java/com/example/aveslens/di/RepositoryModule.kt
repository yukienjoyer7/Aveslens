package com.example.aveslens.di

import com.example.aveslens.data.repository.ObservationRepository
import com.example.aveslens.data.repository.ObservationRepositoryImpl
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
    abstract fun bindObservationRepository(impl: ObservationRepositoryImpl): ObservationRepository
}
