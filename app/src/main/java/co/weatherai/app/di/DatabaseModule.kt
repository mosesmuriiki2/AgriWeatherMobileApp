package co.weatherai.app.di

import android.content.Context
import androidx.room.Room
import co.weatherai.app.data.local.WeatherDao
import co.weatherai.app.data.local.WeatherRoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWeatherDatabase(@ApplicationContext context: Context): WeatherRoomDatabase {
        return Room.databaseBuilder(
            context,
            WeatherRoomDatabase::class.java,
            "weather_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideWeatherDao(db: WeatherRoomDatabase): WeatherDao {
        return db.weatherDao()
    }
}
