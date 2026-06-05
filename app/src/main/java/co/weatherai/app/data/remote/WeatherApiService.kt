package co.weatherai.app.data.remote

import co.weatherai.app.data.model.AgroforestryResponse
import co.weatherai.app.data.model.WeatherResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface WeatherApiService {
    
    @GET("v1/weather")
    suspend fun getWeatherForecast(
        @Header("Authorization") bearerToken: String,
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>

    @GET("v1/usage")
    suspend fun getUsage(
        @Header("Authorization") bearerToken: String
    ): Response<co.weatherai.app.data.model.UsageResponse>

    @GET("v1/weather-geo")
    suspend fun getWeatherGeo(
        @Header("Authorization") bearerToken: String,
        @Query("ip") ip: String = "auto",
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>

    @POST("v1/trees/analyze")
    @Multipart
    suspend fun analyzeForestry(
        @Header("Authorization") bearerToken: String,
        @Part image: MultipartBody.Part,
        @Part("farmerId") farmerId: RequestBody? = null,
        @Part("county") county: RequestBody? = null,
        @Part("landAcres") acreage: RequestBody? = null,
        @Part("location") location: RequestBody? = null,
        @Part("notes") notes: RequestBody? = null
    ): Response<AgroforestryResponse>
}
