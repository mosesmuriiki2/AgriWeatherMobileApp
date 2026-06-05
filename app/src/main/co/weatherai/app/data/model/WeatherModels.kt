package co.weatherai.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    @SerialName("location") val location: LocationInfo,
    @SerialName("current") val current: CurrentInfo,
    @SerialName("hourly") val hourly: List<HourlyInfo>,
    @SerialName("daily") val daily: List<DailyInfo>
)

@Serializable
data class LocationInfo(
    @SerialName("lat") val lat: Double,
    @SerialName("lon") val lon: Double,
    @SerialName("timezone") val timezone: String,
    @SerialName("requested_lat") val requestedLat: Double? = null,
    @SerialName("requested_lon") val requestedLon: Double? = null,
    @SerialName("country") val country: String
)

@Serializable
data class CurrentInfo(
    @SerialName("time") val time: String,
    @SerialName("temperature") val temperature: Double,
    @SerialName("wind_speed") val windSpeed: Double,
    @SerialName("wind_direction") val windDirection: Int? = null,
    @SerialName("condition_code") val conditionCode: String,
    @SerialName("icon") val iconUrl: String? = null,
    @SerialName("icon_path") val iconPath: String
)

@Serializable
data class HourlyInfo(
    @SerialName("time") val time: String,
    @SerialName("temperature") val temperature: Double,
    @SerialName("precipitation_probability") val precipitationProbability: Int,
    @SerialName("wind_speed") val windSpeed: Double,
    @SerialName("condition_code") val conditionCode: String,
    @SerialName("icon") val iconUrl: String? = null,
    @SerialName("humidity") val humidity: Int,
    @SerialName("feels_like") val feelsLike: Double? = null,
    @SerialName("wind_gust") val windGust: Double? = null,
    @SerialName("uv_index") val uvIndex: Double? = null,
    @SerialName("icon_path") val iconPath: String
)

@Serializable
data class DailyInfo(
    @SerialName("date") val date: String,
    @SerialName("temp_min") val tempMin: Double,
    @SerialName("temp_max") val tempMax: Double,
    @SerialName("precipitation_sum") val precipitationSum: Double? = null,
    @SerialName("sunrise") val sunrise: String? = null,
    @SerialName("sunset") val sunset: String? = null,
    @SerialName("condition_code") val conditionCode: String,
    @SerialName("icon") val icon: String? = null,
    @SerialName("precipitation_probability") val precipitationProbability: Int,
    @SerialName("wind_max") val windMax: Double? = null,
    @SerialName("icon_path") val iconPath: String
)
