package co.weatherai.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    @SerialName("location") val location: LocationInfo,
    @SerialName("current") val current: CurrentInfo,
    @SerialName("hourly") val hourly: List<HourlyInfo>,
    @SerialName("daily") val daily: List<DailyInfo>,
    @SerialName("ai_summary") val aiSummary: String? = null
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

@Serializable
data class UsageResponse(
    @SerialName("plan") val plan: String,
    @SerialName("period") val period: UsagePeriod,
    @SerialName("limits") val limits: UsageLimits,
    @SerialName("remaining") val remaining: UsageRemaining
)

@Serializable
data class UsagePeriod(
    @SerialName("start") val start: String,
    @SerialName("end") val end: String,
    @SerialName("requestCount") val requestCount: Int,
    @SerialName("aiRequestCount") val aiRequestCount: Int
)

@Serializable
data class UsageLimits(
    @SerialName("requests") val requests: Int,
    @SerialName("aiRequests") val aiRequests: Int,
    @SerialName("maxDays") val maxDays: Int,
    @SerialName("webhooks") val webhooks: Boolean,
    @SerialName("teamSeats") val teamSeats: Int,
    @SerialName("sms") val sms: Boolean
)

@Serializable
data class UsageRemaining(
    @SerialName("requests") val requests: Int,
    @SerialName("aiRequests") val aiRequests: Int
)

@Serializable
data class AgroforestryResponse(
    @SerialName("analysis_id") val analysisId: String,
    @SerialName("timestamp") val timestamp: String,
    @SerialName("farmer_id") val farmerId: String? = null,
    @SerialName("county") val county: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("land_acres") val landAcres: Double? = null,
    @SerialName("total_tree_count") val totalTreeCount: Int,
    @SerialName("tree_density_per_acre") val treeDensityPerAcre: Double,
    @SerialName("confidence_score") val confidenceScore: Double,
    @SerialName("canopy_coverage_pct") val canopyCoveragePct: Double,
    @SerialName("tree_health") val treeHealth: TreeHealth,
    @SerialName("low_confidence") val lowConfidence: Boolean,
    @SerialName("tree_species_guess") val treeSpeciesGuess: String? = null,
    @SerialName("observations") val observations: List<String>,
    @SerialName("recommendations") val recommendations: List<String>,
    @SerialName("original_image_url") val originalImageUrl: String? = null,
    @SerialName("overlay_image_url") val overlayImageUrl: String? = null,
    @SerialName("cv_debug") val cvDebug: CvDebug? = null
)

@Serializable
data class TreeHealth(
    val healthy: Int,
    @SerialName("needs_care") val needsCare: Int,
    @SerialName("needs_replacement") val needsReplacement: Int
)

@Serializable
data class CvDebug(
    @SerialName("orig_resolution") val origResolution: String,
    @SerialName("work_resolution") val workResolution: String,
    @SerialName("canopy_px") val canopyPx: Long,
    @SerialName("peaks_detected") val peaksDetected: Int,
    @SerialName("after_area_filter") val afterAreaFilter: Int
)
