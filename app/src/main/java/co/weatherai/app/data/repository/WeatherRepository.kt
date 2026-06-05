package co.weatherai.app.data.repository

import android.content.Context
import android.location.Geocoder
import co.weatherai.app.data.local.WeatherDao
import co.weatherai.app.data.local.WeatherCacheEntity
import co.weatherai.app.data.local.ForestryCacheEntity
import co.weatherai.app.data.model.AgroforestryResponse
import co.weatherai.app.data.model.WeatherResponse
import co.weatherai.app.data.remote.WeatherApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val weatherDao: WeatherDao,
    private val apiService: WeatherApiService,
    @Named("WAI_API_KEY") private val apiKey: String
) {
    // 4 hours cache Time-To-Live metric
    private val cacheExpirationMillis = 4 * 60 * 60 * 1000L

    fun getWeatherOfflineFirst(
        lat: Double,
        lon: Double,
        units: String,
        forceRefresh: Boolean = false
    ): Flow<Resource<WeatherResponse>> = flow {
        val cacheKey = "coord_${String.format(java.util.Locale.US, "%.2f", lat)}_${String.format(java.util.Locale.US, "%.2f", lon)}_${units}"
        getWeatherInternal(cacheKey, forceRefresh) {
            apiService.getWeatherForecast(
                bearerToken = "Bearer $apiKey",
                latitude = lat,
                longitude = lon,
                units = units
            )
        }.collect { emit(it) }
    }

    fun getWeatherByIp(): Flow<Resource<WeatherResponse>> = flow {
        val cacheKey = "ip_auto"
        getWeatherInternal(cacheKey, forceRefresh = false) {
            apiService.getWeatherGeo(
                bearerToken = "Bearer $apiKey",
                ip = "auto",
                units = "metric"
            )
        }.collect { emit(it) }
    }

    fun searchWeather(query: String): Flow<Resource<WeatherResponse>> = flow {
        emit(Resource.Loading())
        try {
            val geocoder = Geocoder(context)
            val addresses = withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, 1)
            }
            
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val lat = address.latitude
                val lon = address.longitude
                
                getWeatherOfflineFirst(lat, lon, "metric", forceRefresh = true).collect {
                    emit(it)
                }
            } else {
                emit(Resource.Error(message = "Could not find coordinates for '$query'"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(message = "Search error: ${e.localizedMessage}"))
        }
    }

    private fun getWeatherInternal(
        cacheKey: String,
        forceRefresh: Boolean,
        fetcher: suspend () -> retrofit2.Response<WeatherResponse>
    ): Flow<Resource<WeatherResponse>> = flow {
        emit(Resource.Loading())

        val localEntity = weatherDao.getCachedWeather(cacheKey).first()

        if (localEntity != null) {
            try {
                val cachedResponse = Json.decodeFromString<WeatherResponse>(localEntity.rawJsonPayload)
                emit(Resource.Success(data = cachedResponse, isFromCache = true))
            } catch (err: Exception) {}
        }

        val cacheIsExpired = localEntity == null || (System.currentTimeMillis() - localEntity.timestamp > cacheExpirationMillis)

        if (cacheIsExpired || forceRefresh) {
            try {
                val response = fetcher()

                if (response.isSuccessful && response.body() != null) {
                    val freshData = response.body()!!
                    android.util.Log.d("WeatherRepository", "Successfully fetched fresh data for $cacheKey")
                    val newCache = WeatherCacheEntity(
                        cacheKey = cacheKey,
                        rawJsonPayload = Json.encodeToString(freshData),
                        aiSummaryAdvice = "",
                        timestamp = System.currentTimeMillis()
                    )
                    weatherDao.insertWeatherCache(newCache)
                    emit(Resource.Success(data = freshData, isFromCache = false))
                } else {
                    android.util.Log.e("WeatherRepository", "API Error: ${response.code()} ${response.message()}")
                    if (localEntity != null) {
                        val cachedData = Json.decodeFromString<WeatherResponse>(localEntity.rawJsonPayload)
                        emit(Resource.Success(data = cachedData, isFromCache = true, warning = "Sync issue. Loaded local data."))
                    } else {
                        emit(Resource.Error(message = "Network error: ${response.code()}"))
                    }
                }
            } catch (ex: Exception) {
                if (localEntity != null) {
                    val cachedData = Json.decodeFromString<WeatherResponse>(localEntity.rawJsonPayload)
                    emit(Resource.Success(data = cachedData, isFromCache = true, warning = "Offline Mode"))
                } else {
                    emit(Resource.Error(message = "No connection and no cached data."))
                }
            }
        }
    }

    suspend fun getUsage(): Resource<co.weatherai.app.data.model.UsageResponse> {
        return try {
            val response = apiService.getUsage("Bearer $apiKey")
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to fetch usage: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Usage fetch error: ${e.localizedMessage}")
        }
    }

    suspend fun analyzeForestry(
        imageFile: File,
        farmerId: String? = null,
        county: String? = null,
        landAcres: Double? = null,
        location: String? = null,
        notes: String? = null
    ): Resource<AgroforestryResponse> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            
            val response = apiService.analyzeForestry(
                bearerToken = "Bearer $apiKey",
                image = body,
                farmerId = farmerId?.toRequestBody("text/plain".toMediaTypeOrNull()),
                county = county?.toRequestBody("text/plain".toMediaTypeOrNull()),
                acreage = landAcres?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                location = location?.toRequestBody("text/plain".toMediaTypeOrNull()),
                notes = notes?.toRequestBody("text/plain".toMediaTypeOrNull())
            )
            
            if (response.isSuccessful && response.body() != null) {
                val freshData = response.body()!!
                
                // Cache the analysis result
                val newCache = ForestryCacheEntity(
                    analysisId = freshData.analysisId,
                    farmerId = freshData.farmerId,
                    rawJsonPayload = Json.encodeToString(freshData)
                )
                weatherDao.insertForestryCache(newCache)
                
                Resource.Success(freshData)
            } else {
                Resource.Error("Analysis failed: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Analysis error: ${e.localizedMessage}")
        }
    }

    fun getRecentForestryAnalyses(): Flow<List<AgroforestryResponse>> = flow {
        weatherDao.getRecentForestry().collect { list ->
            val decoded = list.mapNotNull {
                try {
                    Json.decodeFromString<AgroforestryResponse>(it.rawJsonPayload)
                } catch (e: Exception) {
                    null
                }
            }
            emit(decoded)
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
