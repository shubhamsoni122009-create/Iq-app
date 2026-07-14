package com.iqlock.app.di

import android.content.Context
import com.iqlock.app.data.AppDatabase
import com.iqlock.app.data.IQLockRepository
import com.iqlock.app.data.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DatabaseModule.kt — Hilt module that provides the Room database, all DAOs,
 * and the IQLockRepository as singleton-scoped dependencies.
 *
 * Any @HiltViewModel or @AndroidEntryPoint class can inject these by declaring
 * them in the constructor with @Inject.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideRiddleDao(db: AppDatabase): RiddleDao = db.riddleDao()

    @Provides
    @Singleton
    fun provideStatisticDao(db: AppDatabase): StatisticDao = db.statisticDao()

    @Provides
    @Singleton
    fun provideLockHistoryDao(db: AppDatabase): LockHistoryDao = db.lockHistoryDao()

    @Provides
    @Singleton
    fun provideProtectedAppDao(db: AppDatabase): ProtectedAppDao = db.protectedAppDao()

    @Provides
    @Singleton
    fun provideAppSettingsDao(db: AppDatabase): AppSettingsDao = db.appSettingsDao()

    @Provides
    @Singleton
    fun provideRepository(
        riddleDao: RiddleDao,
        statisticDao: StatisticDao,
        lockHistoryDao: LockHistoryDao,
        protectedAppDao: ProtectedAppDao,
        appSettingsDao: AppSettingsDao
    ): IQLockRepository = IQLockRepository(
        riddleDao, statisticDao, lockHistoryDao, protectedAppDao, appSettingsDao
    )
}
