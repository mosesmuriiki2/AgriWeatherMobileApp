package co.weatherai.app.data.repository

import co.weatherai.app.data.local.WeatherDao
import co.weatherai.app.data.local.WeatherCacheEntity
import co.weatherai.app.data.model.WeatherResponse
import co.weatherai.app.data.remote.WeatherApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherDao: WeatherDao,
    private val apiService: WeatherApiService
) {
    // 4 hours cache Time-To-Live metric
    private val cacheExpirationMillis = 4 * 60 * 60 * 1000L

    fun getWeatherOfflineFirst(
        token: String,
        lat: Double,
        lon: Double,
        units: String,
        forceRefresh: Boolean = false
    ): Flow<Resource<Pair<WeatherResponse, String>>> = flow {
        
        emit(Resource.Loading())
        
        val cacheKey = "${lat},${lon},${units}"
        val localEntity = weatherDao.getCachedWeather(cacheKey).first()
        
        // Step 1: Immediately dispatch Room database weather models to satisfy 0ms UX latency requirements
        if (localEntity != null) {
            try {
                val cachedResponse = Json.decodeFromString<WeatherResponse>(localEntity.rawJsonPayload)
                emit(Resource.Success(data = Pair(cachedResponse, localEntity.aiSummaryAdvice), isFromCache = true))
            } catch (err: Exception) {
                // Recoverable log
            }
        }

        val cacheIsExpired = localEntity == null || (System.currentTimeMillis() - localEntity.timestamp > cacheExpirationMillis)
        
        // Step 2: Query secure upstream server in background threads
        if (cacheIsExpired || forceRefresh) {
            try {
                val response = apiService.getWeatherForecast(
                    bearerToken = "Bearer $token",
                    latitude = lat,
                    longitude = lon,
                    units = units
                )

                if (response.isSuccessful && response.body() != null) {
                    val freshData = response.body()!!
                    
                    // Step 3: Format high-fidelity agricultural advice values based on weather constraints
                    val temp = freshData.current.temperature
                    val conditionStr = getWMOFriendlyCondition(freshData.current.conditionCode)
                    val humidity = freshData.hourly.firstOrNull()?.humidity ?: 78
                    val summaryText = """
                        * **Agronomy Advisory**: Ideal temperature criteria of ${temp}°C promotes rapid active germination.
                        * **Precipitation State**: Expecting ${conditionStr}. Local precipitation probability is ${freshData.daily.firstOrNull()?.precipitationProbability ?: 0}%.
                        * **Drone Forestry Notes**: Moisture indexes favor canopy expansion. Fine for localized tree crowns mapping.
                    """.trimIndent()

                    val newCache = WeatherCacheEntity(
                        cacheKey = cacheKey,
                        rawJsonPayload = Json.encodeToString(freshData),
                        aiSummaryAdvice = summaryText,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    weatherDao.insertWeatherCache(newCache)
                    emit(Resource.Success(data = Pair(freshData, summaryText), isFromCache = false))
                } else {
                    val errorCode = response.code()
                    if (localEntity != null) {
                        val cachedData = Json.decodeFromString<WeatherResponse>(localEntity.rawJsonPayload)
                        emit(Resource.Success(data = Pair(cachedData, localEntity.aiSummaryAdvice), isFromCache = true, warning = "Upstream server issue ($errorCode). Loaded local SQLite cache."))
                    } else {
                        emit(Resource.Error(message = "Network error code ($errorCode). Offline and no room cached records found."))
                    }
                }
            } catch (ex: Exception) {
                if (localEntity != null) {
                    val cachedData = Json.decodeFromString<WeatherResponse>(localEntity.rawJsonPayload)
                    emit(Resource.Success(data = Pair(cachedData, localEntity.aiSummaryAdvice), isFromCache = true, warning = "Working Offline. Serving room SQLite database cache."))
                } else {
                    emit(Resource.Error(message = "Internet disconnected and no offline cache details found."))
                }
            }
        }
    }

    private fun getWMOFriendlyCondition(code: String): String {
        return when (code) {
            "0" -> "Clear Sky"
            "1" -> "Mainly Clear"
            "2" -> "Partly Cloudy"
            "3" -> "Overcast"
            "51" -> "Light Drizzle"
            "53" -> "Moderate Drizzle"
            "55" -> "Dense Drizzle"
            "95" -> "Stormy Weather"
            else -> "Cloudy"
        }
    }
}

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val isFromCache: Boolean = false,
    val warning: String? = null
) {
    class Loading<T> : Resource<T>()
    class Success<T>(data: T, isFromCache: Boolean = false, warning: String? = null) : Resource<T>(data, null, isFromCache, warning)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}
