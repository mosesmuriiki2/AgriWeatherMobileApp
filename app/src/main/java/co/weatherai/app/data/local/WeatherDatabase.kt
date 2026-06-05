package co.weatherai.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val cacheKey: String, // format: "lat,lon,units"
    val rawJsonPayload: String, // Exquisite: caches the entire WeatherResponse structure locally
    val aiSummaryAdvice: String, // Caches generated agronomy advice alongside
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "forestry_cache")
data class ForestryCacheEntity(
    @PrimaryKey val analysisId: String,
    val farmerId: String?,
    val rawJsonPayload: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE cacheKey = :key LIMIT 1")
    fun getCachedWeather(key: String): Flow<WeatherCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherCache(cache: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE cacheKey = :key")
    suspend fun deleteCache(key: String)

    @Query("DELETE FROM weather_cache")
    suspend fun clearAllCache()

    @Query("SELECT * FROM forestry_cache ORDER BY timestamp DESC")
    fun getRecentForestry(): Flow<List<ForestryCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForestryCache(cache: ForestryCacheEntity)
}

@Database(entities = [WeatherCacheEntity::class, ForestryCacheEntity::class], version = 2, exportSchema = false)
abstract class WeatherRoomDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}
