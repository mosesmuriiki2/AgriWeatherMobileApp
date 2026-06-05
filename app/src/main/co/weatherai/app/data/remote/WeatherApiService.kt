package co.weatherai.app.data.remote

import co.weatherai.app.data.model.WeatherResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface WeatherApiService {
    
    @GET("v1/weather")
    suspend fun getWeatherForecast(
        @Header("Authorization") bearerToken: String, // Value: "Bearer wai_..."
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>

    @POST("v1/trees/analyze")
    @Multipart
    suspend fun analyzeForestry(
        @Header("Authorization") bearerToken: String,
        @Part image: MultipartBody.Part,
        @Part("landAcres") acreage: RequestBody
    ): Response<RequestBody>
}
